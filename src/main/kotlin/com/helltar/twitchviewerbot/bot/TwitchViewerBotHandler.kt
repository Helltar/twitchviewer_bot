package com.helltar.twitchviewerbot.bot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.commands.CommandRegistry
import com.annimon.tgbotsmodule.commands.SimpleCommand
import com.annimon.tgbotsmodule.commands.authority.SimpleAuthority
import com.helltar.twitchviewerbot.EnvConfig
import com.helltar.twitchviewerbot.EnvConfig.botToken
import com.helltar.twitchviewerbot.EnvConfig.creatorId
import com.helltar.twitchviewerbot.RequestExecutor.addRequest
import com.helltar.twitchviewerbot.command.BotCommand
import com.helltar.twitchviewerbot.command.Commands.COMMAND_ABOUT
import com.helltar.twitchviewerbot.command.Commands.COMMAND_ADD
import com.helltar.twitchviewerbot.command.Commands.COMMAND_CLIP
import com.helltar.twitchviewerbot.command.Commands.COMMAND_LIST
import com.helltar.twitchviewerbot.command.Commands.COMMAND_LIVE
import com.helltar.twitchviewerbot.command.Commands.COMMAND_SCREENSHOT
import com.helltar.twitchviewerbot.command.Commands.COMMAND_START
import com.helltar.twitchviewerbot.command.Commands.COMMAND_UPTIME
import com.helltar.twitchviewerbot.command.commands.*
import com.helltar.twitchviewerbot.dao.DatabaseFactory
import com.helltar.twitchviewerbot.keyboard.KeyboardBundle
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update

class TwitchViewerBotHandler : BotHandler(botToken) {

    private val authority = SimpleAuthority(creatorId)
    private val commands = CommandRegistry(botUsername, authority)

    private val log = LoggerFactory.getLogger(javaClass)

    init {
        commands.run {
            register(SimpleCommand("/add") { runCommand(AddCommand(it), COMMAND_ADD) })
            register(SimpleCommand("/clip") { runCommand(ClipCommand(it), COMMAND_CLIP) })
            register(SimpleCommand("/live") { runCommand(LiveCommand(it), COMMAND_LIVE) })
            register(SimpleCommand("/screen") { runCommand(ScreenCommand(it), COMMAND_SCREENSHOT) })
            register(SimpleCommand("/list") { runCommand(ListCommand(it), COMMAND_LIST) })

            register(SimpleCommand("/start") { runCommand(StartCommand(it), COMMAND_START) })
            register(SimpleCommand("/help") { runCommand(HelpCommand(it), COMMAND_START) })
            register(SimpleCommand("/uptime") { runCommand(UptimeCommand(it), COMMAND_UPTIME) })
            register(SimpleCommand("/about") { runCommand(AboutCommand(it), COMMAND_ABOUT) })

            registerBundle(KeyboardBundle())
        }
    }

    override fun onUpdate(update: Update): BotApiMethod<*>? {
        commands.handleUpdate(this, update)
        return null
    }

    private fun runCommand(botCommand: BotCommand, requestKey: String) {
        val user = botCommand.ctx.user()
        val userId = user.id
        val chat = botCommand.ctx.message().chat
        val chatId = chat.id
        val commandName = botCommand.javaClass.simpleName

        log.info("$commandName: $chatId $userId ${user.userName} ${user.firstName} ${chat.title} : ${botCommand.argumentsAsString}")

        addRequest("$requestKey@$userId", botCommand.ctx) {
            if (!DatabaseFactory.usersDAO.isUserExists(userId))
                DatabaseFactory.usersDAO.addUser(user)
            else
                DatabaseFactory.usersDAO.updateUserData(user)

            botCommand.run()
        }
    }

    override fun getBotUsername() =
        EnvConfig.botUsername
}