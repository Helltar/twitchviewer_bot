package com.helltar.twitchviewer_bot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.commands.CommandRegistry
import com.annimon.tgbotsmodule.commands.SimpleCommand
import com.annimon.tgbotsmodule.commands.authority.SimpleAuthority
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewer_bot.BotConfig.BOT_TOKEN
import com.helltar.twitchviewer_bot.BotConfig.BOT_USERNAME
import com.helltar.twitchviewer_bot.BotConfig.CREATOR_ID
import com.helltar.twitchviewer_bot.TwitchViewerBot.Companion.addRequest
import com.helltar.twitchviewer_bot.commands.BotCommand
import com.helltar.twitchviewer_bot.commands.Commands.COMMAND_ABOUT
import com.helltar.twitchviewer_bot.commands.Commands.COMMAND_ADD
import com.helltar.twitchviewer_bot.commands.Commands.COMMAND_CLIP
import com.helltar.twitchviewer_bot.commands.Commands.COMMAND_CLIP_COMPRESS
import com.helltar.twitchviewer_bot.commands.Commands.COMMAND_LIST
import com.helltar.twitchviewer_bot.commands.Commands.COMMAND_LIVE
import com.helltar.twitchviewer_bot.commands.Commands.COMMAND_SCREENSHOT
import com.helltar.twitchviewer_bot.commands.Commands.COMMAND_START
import com.helltar.twitchviewer_bot.commands.Commands.COMMAND_UPTIME
import com.helltar.twitchviewer_bot.commands.commands.*
import com.helltar.twitchviewer_bot.db.Databases
import com.helltar.twitchviewer_bot.keyboard.KeyboardBundle
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update

class TwitchViewerBotHandler : BotHandler(BOT_TOKEN) {

    private val authority = SimpleAuthority(CREATOR_ID)
    private val commands = CommandRegistry(BOT_USERNAME, authority)

    private val log = LoggerFactory.getLogger(javaClass)

    init {
        commands.run {
            register(SimpleCommand("/add") { runCommand(AddCommand(it, it.arguments().toList()), COMMAND_ADD) })
            register(SimpleCommand("/clip") { runCommand(ClipCommand(it, it.arguments().toList()), COMMAND_CLIP) })
            register(SimpleCommand("/live") { runCommand(LiveCommand(it, it.arguments().toList()), COMMAND_LIVE) })
            register(SimpleCommand("/screen") { runCommand(ScreenCommand(it, it.arguments().toList()), COMMAND_SCREENSHOT) })

            register(SimpleCommand("/about") { runCommand(AboutCommand(it), COMMAND_ABOUT) })
            register(SimpleCommand("/help") { runCommand(HelpCommand(it), COMMAND_START) })
            register(SimpleCommand("/list") { runCommand(ListCommand(it), COMMAND_LIST) })
            register(SimpleCommand("/start") { runCommand(StartCommand(it), COMMAND_START) })
            register(SimpleCommand("/uptime") { runCommand(UptimeCommand(it), COMMAND_UPTIME) })

            registerBundle(KeyboardBundle())
        }
    }

    override fun onUpdate(update: Update): BotApiMethod<*>? {
        if (update.hasMessage() && update.message.isReply) {
            if (update.message.replyToMessage.from.id == this.me.id) {
                val ctx = MessageContext(this, update, "")
                runCommand(ClipCompressCommand(ctx), COMMAND_CLIP_COMPRESS)
            }
        }

        commands.handleUpdate(this, update)

        return null
    }

    private fun runCommand(botCommand: BotCommand, requestKey: String) {
        val user = botCommand.ctx.user()
        val userId = user.id
        val chat = botCommand.ctx.message().chat
        val chatId = chat.id
        val commandName = botCommand.javaClass.simpleName

        log.info("$commandName: $chatId $userId ${user.userName} ${user.firstName} ${chat.title} : ${botCommand.args}")

        Databases.dbUsers.saveUserData(user)

        addRequest("$requestKey@$userId", botCommand.ctx) { botCommand.run() }
    }

    override fun getBotUsername() =
        BOT_USERNAME
}