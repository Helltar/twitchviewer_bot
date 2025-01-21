package com.helltar.twitchviewerbot.bot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.commands.CommandRegistry
import com.annimon.tgbotsmodule.commands.SimpleCommand
import com.annimon.tgbotsmodule.commands.authority.SimpleAuthority
import com.helltar.twitchviewerbot.Config.botUsername
import com.helltar.twitchviewerbot.Config.creatorId
import com.helltar.twitchviewerbot.commands.CommandExecutor
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_ABOUT
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_ADD
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_CLIP
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_LIST
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_LIVE
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_PRIVACY
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_SCREENSHOT
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_START
import com.helltar.twitchviewerbot.commands.simple.*
import com.helltar.twitchviewerbot.commands.system.UpdatePrivacyPolicy
import com.helltar.twitchviewerbot.commands.twitch.*
import com.helltar.twitchviewerbot.commands.twitch.keyboard.KeyboardBundle
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update

class TwitchViewerBotHandler(botModuleOptions: BotModuleOptions) : BotHandler(botModuleOptions) {

    private val authority = SimpleAuthority(creatorId)
    private val commandRegistry = CommandRegistry(botUsername, authority)

    private val commandExecutor = CommandExecutor()

    init {
        commandRegistry.run {
            register(SimpleCommand("/add") { commandExecutor.executeCommand(AddCommand(it), COMMAND_ADD) })
            register(SimpleCommand("/clip") { commandExecutor.executeCommand(ClipCommand(it), COMMAND_CLIP) })
            register(SimpleCommand("/live") { commandExecutor.executeCommand(LiveCommand(it), COMMAND_LIVE) })
            register(SimpleCommand("/screen") { commandExecutor.executeCommand(ScreenCommand(it), COMMAND_SCREENSHOT) })
            register(SimpleCommand("/list") { commandExecutor.executeCommand(ListCommand(it), COMMAND_LIST) })
            register(SimpleCommand("/cancel") { commandExecutor.cancelJobs(it) })

            register(SimpleCommand("/start") { commandExecutor.executeCommand(StartCommand(it), COMMAND_START) })
            register(SimpleCommand("/help") { commandExecutor.executeCommand(HelpCommand(it), COMMAND_START) })
            register(SimpleCommand("/about") { commandExecutor.executeCommand(AboutCommand(it), COMMAND_ABOUT) })

            register(SimpleCommand("/updateprivacy") { commandExecutor.executeCommand(UpdatePrivacyPolicy(it), COMMAND_PRIVACY) })
            register(SimpleCommand("/privacy") { commandExecutor.executeCommand(PrivacyCommand(it), COMMAND_PRIVACY) })

            registerBundle(KeyboardBundle())
        }
    }

    override fun onUpdate(update: Update): BotApiMethod<*>? {
        commandRegistry.handleUpdate(this, update)
        return null
    }
}
