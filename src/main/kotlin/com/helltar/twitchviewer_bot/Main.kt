package com.helltar.twitchviewer_bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.dispatcher.telegramError
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.github.kotlintelegrambot.logging.LogLevel
import com.helltar.twitchviewer_bot.BotConfig.BOT_TOKEN
import com.helltar.twitchviewer_bot.commands.BotCommand
import com.helltar.twitchviewer_bot.commands.Commands.commandAbout
import com.helltar.twitchviewer_bot.commands.Commands.commandAdd
import com.helltar.twitchviewer_bot.commands.Commands.commandClip
import com.helltar.twitchviewer_bot.commands.Commands.commandClipCompress
import com.helltar.twitchviewer_bot.commands.Commands.commandList
import com.helltar.twitchviewer_bot.commands.Commands.commandLive
import com.helltar.twitchviewer_bot.commands.Commands.commandScreenshot
import com.helltar.twitchviewer_bot.commands.Commands.commandStart
import com.helltar.twitchviewer_bot.commands.Commands.commandUptime
import com.helltar.twitchviewer_bot.commands.commands.*
import com.helltar.twitchviewer_bot.db.Databases
import com.helltar.twitchviewer_bot.db.Databases.dbUsers
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonBack
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonChannel
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonClip
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonClips
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonCloseList
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonDeleteChannel
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonLive
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonScreenshot
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonShow
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.getButtonNameFromCbData
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.getChannelNameFromCbData
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.getChannelStatusFromCbData
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.getOwnerIdFromCbData
import com.helltar.twitchviewer_bot.keyboard.InlineKeyboard
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("twitchviewer_bot")

private val requestList = hashMapOf<String, Job>()
private const val REQUEST_KEY_DELIMITER = "@"

private fun main() {

    Databases.init()

    log.info("start ...")

    bot {
        token = BOT_TOKEN
        logLevel = LogLevel.Error

        dispatch {
            command("add") { runCommand(AddCommand(bot, update.message!!, args), commandAdd) }
            command("clip") { runCommand(ClipCommand(bot, update.message!!, args), commandClip) }
            command("list") { runCommand(ListCommand(bot, update.message!!), commandList) }
            command("live") { runCommand(LiveCommand(bot, update.message!!, args), commandLive) }
            command("screen") { runCommand(ScreenCommand(bot, update.message!!, args), commandScreenshot) }

            command("start") { runCommand(StartCommand(bot, update.message!!), commandStart) }
            command("help") { runCommand(HelpCommand(bot, update.message!!), commandStart) }

            command("uptime") { runCommand(UptimeCommand(bot, update.message!!), commandUptime) }
            command("about") { runCommand(AboutCommand(bot, update.message!!), commandAbout) }

            message(Filter.Reply) {
                val botId = update.message!!.replyToMessage!!.from!!.id

                if (botId == 5605702829L)
                    runCommand(ClipCompressCommand(bot, update.message!!), commandClipCompress)
            }

            callbackQuery {
                val message = callbackQuery.message ?: return@callbackQuery
                val ownerId = getOwnerIdFromCbData(callbackQuery.data) ?: return@callbackQuery
                val userId = callbackQuery.from.id
                val username = callbackQuery.from.username ?: callbackQuery.from.firstName

                if (callbackQuery.from.id != ownerId) {
                    bot.answerCallbackQuery(
                        callbackQuery.id,
                        String.format(localizedString(Strings.dont_touch_is_not_your_list, userId), username)
                    )

                    return@callbackQuery
                }

                val buttonName = getButtonNameFromCbData(callbackQuery.data)

                if (buttonName != buttonCloseList)
                    runKeyboardCommand(buttonName, bot, message, ownerId, callbackQuery.data)
                else
                    bot.editMessageText(
                        ChatId.fromId(message.chat.id), message.messageId,
                        text = String.format(localizedString(Strings.user_close_list, ownerId), username),
                        parseMode = ParseMode.HTML
                    )
            }

            telegramError { log.error(error.getErrorMessage()) }
        }
    }
        .startPolling()
}

private fun runKeyboardCommand(buttonName: String, bot: Bot, message: Message, ownerId: Long, callbackData: String) {
    val twitchChannel = getChannelNameFromCbData(callbackData)

    addRequest(buttonName + REQUEST_KEY_DELIMITER + ownerId, bot, message, ownerId) {
        InlineKeyboard(bot, message, ownerId).run {
            when (buttonName) {
                buttonBack -> update()
                buttonChannel -> btnChannel(twitchChannel, getChannelStatusFromCbData(callbackData))
                buttonClip -> btnClip(twitchChannel)
                buttonClips -> btnClips()
                buttonDeleteChannel -> btnDeleteChannel(twitchChannel)
                buttonLive -> btnLive()
                buttonScreenshot -> btnScreenshot(twitchChannel)
                buttonShow -> btnShow(twitchChannel)
            }
        }
    }
}

private fun runCommand(botCommand: BotCommand, requestKey: String) {
    val user = botCommand.message.from ?: return
    val userId = user.id
    val chat = botCommand.message.chat
    val chatId = chat.id
    val commandName = botCommand.javaClass.simpleName

    log.info("$commandName: $chatId $userId ${user.username} ${user.firstName} ${chat.title} : ${botCommand.args}")

    dbUsers.saveUserData(user)

    addRequest(requestKey + REQUEST_KEY_DELIMITER + userId, botCommand.bot, botCommand.message, userId) {
        botCommand.run()
    }
}

private fun addRequest(requestKey: String, bot: Bot, message: Message, userId: Long, func: () -> Unit) {
    if (requestList.containsKey(requestKey))
        if (requestList[requestKey]?.isCompleted == false) {
            bot.sendMessage(
                ChatId.fromId(message.chat.id),
                localizedString(Strings.many_request, userId),
                replyToMessageId = message.messageId, allowSendingWithoutReply = true
            )

            return
        }

    requestList[requestKey] = CoroutineScope(Dispatchers.Default).launch { func() }
}
