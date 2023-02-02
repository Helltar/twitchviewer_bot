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
import com.helltar.twitchviewer_bot.keyboard.KeyboardBundle
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

class TwitchViewerBotHandler : BotHandler() {

    private val authority = SimpleAuthority(this, CREATOR_ID)
    private val commands = CommandRegistry(this, authority)

    init {
        commands.run {
            register(SimpleCommand("/add") { runCommand(AddCommand(it, it.arguments().toList()), commandAdd) })
            register(SimpleCommand("/clip") { runCommand(ClipCommand(it, it.arguments().toList()), commandClip) })
            register(SimpleCommand("/live") { runCommand(LiveCommand(it, it.arguments().toList()), commandLive) })
            register(SimpleCommand("/screen") { runCommand(ScreenCommand(it, it.arguments().toList()), commandScreenshot) })

            register(SimpleCommand("/about") { runCommand(AboutCommand(it), commandAbout) })
            register(SimpleCommand("/help") { runCommand(HelpCommand(it), commandStart) })
            register(SimpleCommand("/list") { runCommand(ListCommand(it), commandList) })
            register(SimpleCommand("/start") { runCommand(StartCommand(it), commandStart) })
            register(SimpleCommand("/uptime") { runCommand(UptimeCommand(it), commandUptime) })

            registerBundle(KeyboardBundle())
        }

        addMethodPreprocessor(SendMessage::class.java) {
            it.allowSendingWithoutReply = true
            it.enableHtml(true)
            it.disableWebPagePreview()
        }
    }

    override fun onUpdate(update: Update): BotApiMethod<*>? {
        if (update.hasMessage() && update.message.isReply) {
            if (update.message.replyToMessage.from.id == this.me.id) {
                val ctx = MessageContext(this, update, "")
                runCommand(ClipCompressCommand(ctx), commandClipCompress)
            }
        }

        commands.handleUpdate(update)

        return null
    }

    private fun runCommand(botCommand: BotCommand, requestKey: String) {
        val user = botCommand.ctx.user()
        val userId = user.id
        val chat = botCommand.ctx.message().chat
        val chatId = chat.id
        val commandName = botCommand.javaClass.simpleName

        LoggerFactory.getLogger(javaClass)
            .info("$commandName: $chatId $userId ${user.userName} ${user.firstName} ${chat.title} : ${botCommand.args}")

        Databases.dbUsers.saveUserData(user)

        addRequest("$requestKey@$userId", botCommand.ctx) { botCommand.run() }
    }

    override fun getBotToken() = BOT_TOKEN

    override fun getBotUsername() = BOT_USERNAME
}
