package com.helltar.twitchviewerbot

import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.Runner
import com.annimon.tgbotsmodule.beans.Config
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.BotConfig.DIR_DB
import com.helltar.twitchviewerbot.Strings.localizedString
import com.helltar.twitchviewerbot.dao.DatabaseFactory
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess

class TwitchViewerBot : BotModule {

    companion object {
        private val requestsList = hashMapOf<String, Job>()

        @JvmStatic
        fun main(args: Array<String>) {
            val databaseDir = File(DIR_DB)

            if (!databaseDir.exists() && !databaseDir.mkdir())
                throw RuntimeException("error when create dir: $DIR_DB")

            DatabaseFactory.init()

            LoggerFactory.getLogger(TwitchViewerBot::class.java).info("start ...")

            Runner.run("", listOf(TwitchViewerBot()))
        }

        fun addRequest(requestKey: String, ctx: MessageContext, block: () -> Unit) {
            if (requestsList.containsKey(requestKey))
                if (requestsList[requestKey]?.isCompleted == false) {
                    ctx.replyToMessage().setText(localizedString(Strings.many_request, ctx.user().id)).callAsync(ctx.sender)
                    return
                }

            requestsList[requestKey] = CoroutineScope(Dispatchers.IO).launch(CoroutineName(requestKey)) { block() }
        }
    }

    override fun botHandler(config: Config) =
        TwitchViewerBotHandler()
}