package com.helltar.twitchviewerbot.bot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.commands.CommandRegistry
import com.annimon.tgbotsmodule.commands.SimpleCommand
import com.annimon.tgbotsmodule.commands.authority.SimpleAuthority
import com.helltar.twitchviewerbot.EnvConfig.botUsername
import com.helltar.twitchviewerbot.EnvConfig.creatorId
import com.helltar.twitchviewerbot.commands.CommandExecutor
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_ABOUT
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_ADD
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_CLIP
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_LIST
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_LIVE
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_PRIVACY
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_SCREENSHOT
import com.helltar.twitchviewerbot.commands.CommandExecutor.Companion.COMMAND_START
import com.helltar.twitchviewerbot.commands.simple.AboutCommand
import com.helltar.twitchviewerbot.commands.simple.HelpCommand
import com.helltar.twitchviewerbot.commands.simple.PrivacyCommand
import com.helltar.twitchviewerbot.commands.simple.StartCommand
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
            register(SimpleCommand("/add") { commandExecutor.execute(AddCommand(it), COMMAND_ADD) })
            register(SimpleCommand("/clip") { commandExecutor.execute(ClipCommand(it), COMMAND_CLIP) })
            register(SimpleCommand("/live") { commandExecutor.execute(LiveCommand(it), COMMAND_LIVE) })
            register(SimpleCommand("/screen") { commandExecutor.execute(ScreenCommand(it), COMMAND_SCREENSHOT) })
            register(SimpleCommand("/list") { commandExecutor.execute(ListCommand(it), COMMAND_LIST) })

            register(SimpleCommand("/start") { commandExecutor.execute(StartCommand(it), COMMAND_START) })
            register(SimpleCommand("/help") { commandExecutor.execute(HelpCommand(it), COMMAND_START) })
            register(SimpleCommand("/about") { commandExecutor.execute(AboutCommand(it), COMMAND_ABOUT) })

            register(SimpleCommand("/updateprivacy") { commandExecutor.execute(UpdatePrivacyPolicy(it), COMMAND_PRIVACY) })
            register(SimpleCommand("/privacy") { commandExecutor.execute(PrivacyCommand(it), COMMAND_PRIVACY) })

            registerBundle(KeyboardBundle())
        }
    }

    override fun onUpdate(update: Update): BotApiMethod<*>? {
        commandRegistry.handleUpdate(this, update)
        return null
    }
}