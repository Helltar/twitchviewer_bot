package com.helltar.twitchviewerbot

import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.Runner
import com.annimon.tgbotsmodule.beans.Config
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.BotConfig.DIR_DB
import com.helltar.twitchviewerbot.db.Databases
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File

class TwitchViewerBot : BotModule {

    companion object {
        private val commandRequestsList = hashMapOf<String, Job>()

        @JvmStatic
        fun main(args: Array<String>) {
            val databaseDir = File(DIR_DB)

            if (!databaseDir.exists() && !databaseDir.mkdir())
                throw RuntimeException("Error when create dir: $DIR_DB")

            Databases.init()

            LoggerFactory.getLogger(TwitchViewerBot::class.java).info("start ...")

            Runner.run("", listOf(TwitchViewerBot()))
        }

        fun addRequest(requestKey: String, ctx: MessageContext, func: () -> Unit) {
            if (commandRequestsList.containsKey(requestKey))
                if (commandRequestsList[requestKey]?.isCompleted == false) {
                    ctx.replyToMessage().setText(localizedString(Strings.many_request, ctx.user().id)).callAsync(ctx.sender)
                    return
                } // todo: remove completed

            commandRequestsList[requestKey] = CoroutineScope(Dispatchers.IO)
                .launch(CoroutineName(requestKey)) {
                    func()
                }
        }
    }

    override fun botHandler(config: Config) =
        TwitchViewerBotHandler()
}