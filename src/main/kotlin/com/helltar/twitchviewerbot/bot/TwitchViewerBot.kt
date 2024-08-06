package com.helltar.twitchviewerbot.bot

import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.Runner
import com.annimon.tgbotsmodule.beans.Config
import com.helltar.twitchviewerbot.EnvConfig.botToken
import com.helltar.twitchviewerbot.db.DatabaseFactory

class TwitchViewerBot : BotModule {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            DatabaseFactory.init()
            Runner.run("", listOf(TwitchViewerBot()))
        }
    }

    override fun botHandler(config: Config) =
        TwitchViewerBotHandler(BotModuleOptions.createDefault(botToken))
}