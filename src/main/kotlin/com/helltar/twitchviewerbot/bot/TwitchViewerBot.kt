package com.helltar.twitchviewerbot.bot

import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.Runner
import com.annimon.tgbotsmodule.beans.Config
import com.helltar.twitchviewerbot.Config.DIR_DB
import com.helltar.twitchviewerbot.dao.DatabaseFactory
import java.io.File

class TwitchViewerBot : BotModule {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val databaseDir = File(DIR_DB)

            if (!databaseDir.exists() && !databaseDir.mkdir())
                throw RuntimeException("error when create dir: $DIR_DB")

            DatabaseFactory.init()

            Runner.run("", listOf(TwitchViewerBot()))
        }
    }

    override fun botHandler(config: Config) =
        TwitchViewerBotHandler()
}