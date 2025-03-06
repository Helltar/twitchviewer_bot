package com.helltar.twitchviewerbot.bot

import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.Runner
import com.annimon.tgbotsmodule.beans.Config
import com.helltar.twitchviewerbot.Config.botToken
import com.helltar.twitchviewerbot.database.Database

class TwitchViewerBot : BotModule {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Database.init()
            Runner.run("", listOf(TwitchViewerBot()))
        }
    }

    override fun botHandler(config: Config) =
        TwitchViewerBotHandler(BotModuleOptions.createDefault(botToken))
}
