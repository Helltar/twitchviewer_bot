package com.helltar.twitchviewer_bot

import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.Runner
import com.annimon.tgbotsmodule.beans.Config
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewer_bot.BotConfig.DIR_DB
import com.helltar.twitchviewer_bot.db.Databases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.io.File

private val requestList = hashMapOf<String, Job>()

class TwitchViewerBot : BotModule {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val databaseDir = File(DIR_DB)

            if (!databaseDir.exists() && !databaseDir.mkdir())
                throw RuntimeException("Error when create dir: $DIR_DB")

            Databases.init()

            LoggerFactory.getLogger(TwitchViewerBot::class.java).info("start ...")

            val profile = args.firstOrNull() ?: ""
            Runner.run(profile, listOf(TwitchViewerBot()))
        }

        fun addRequest(requestKey: String, ctx: MessageContext, func: () -> Unit) {
            if (requestList.containsKey(requestKey))
                if (requestList[requestKey]?.isCompleted == false) {
                    ctx.replyToMessage()
                        .setText(localizedString(Strings.many_request, ctx.user().id))
                        .callAsync(ctx.sender)
                    return
                }

            requestList[requestKey] = CoroutineScope(Dispatchers.Default).launch { func() }
        }
    }

    override fun botHandler(config: Config) = TwitchViewerBotHandler()
}
