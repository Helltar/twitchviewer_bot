package com.helltar.twitchviewerbot.bot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.commands.CommandRegistry
import com.annimon.tgbotsmodule.commands.SimpleCommand
import com.annimon.tgbotsmodule.commands.authority.SimpleAuthority
import com.helltar.twitchviewerbot.Config.botUsername
import com.helltar.twitchviewerbot.Config.creatorId
import com.helltar.twitchviewerbot.bot.CommandExecutor.cancelJobs
import com.helltar.twitchviewerbot.bot.CommandExecutor.executeCommand
import com.helltar.twitchviewerbot.commands.simple.AboutCommand
import com.helltar.twitchviewerbot.commands.simple.HelpCommand
import com.helltar.twitchviewerbot.commands.simple.StartCommand
import com.helltar.twitchviewerbot.commands.twitch.AddCommand
import com.helltar.twitchviewerbot.commands.twitch.ClipCommand
import com.helltar.twitchviewerbot.commands.twitch.ListCommand
import com.helltar.twitchviewerbot.commands.twitch.ScreenshotCommand
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.BUTTON_CLIPS
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.BUTTON_SCREEN
import com.helltar.twitchviewerbot.commands.twitch.keyboard.KeyboardBundle
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update

class TwitchViewerBotHandler(botModuleOptions: BotModuleOptions) : BotHandler(botModuleOptions) {

    private val authority = SimpleAuthority(creatorId)
    private val commandRegistry = CommandRegistry(botUsername, authority)

    init {
        commandRegistry.run {
            register(SimpleCommand("/start") { executeCommand(StartCommand(it)) })
            register(SimpleCommand("/help") { executeCommand(HelpCommand(it)) })
            register(SimpleCommand("/about") { executeCommand(AboutCommand(it)) })

            register(SimpleCommand("/clip") { executeCommand(ClipCommand(it), BUTTON_CLIPS) })
            register(SimpleCommand("/screenshot") { executeCommand(ScreenshotCommand(it), BUTTON_SCREEN) })
            register(SimpleCommand("/add") { executeCommand(AddCommand(it)) })
            register(SimpleCommand("/list") { executeCommand(ListCommand(it)) })
            register(SimpleCommand("/cancel") { cancelJobs(it) })

            registerBundle(KeyboardBundle())
        }
    }

    override fun onUpdate(update: Update): BotApiMethod<*>? {
        commandRegistry.handleUpdate(this, update)
        return null
    }
}
