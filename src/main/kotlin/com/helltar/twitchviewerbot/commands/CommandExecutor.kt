package com.helltar.twitchviewerbot.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.db.dao.usersDao
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.telegram.telegrambots.meta.api.methods.ParseMode
import java.util.concurrent.ConcurrentHashMap

object CommandExecutor {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val requestsMap = ConcurrentHashMap<String, Job>()

    private val log = KotlinLogging.logger {}

    fun executeCommand(botCommand: BotCommand, requestKey: String? = null) {
        val user = botCommand.ctx.user()
        val userId = user.id
        val chat = botCommand.ctx.message().chat
        val commandName = botCommand.javaClass.simpleName

        log.info { "$commandName: ${chat.id} $userId ${user.userName} ${user.firstName} ${chat.title}: ${botCommand.ctx.message().text}" }

        val launch =
            launch("${requestKey ?: commandName}@$userId") {
                if (!usersDao.add(user)) usersDao.update(user)
                botCommand.run()
            }

        if (!launch)
            botCommand.replyToMessage(Strings.localizedString(Strings.MANY_REQUEST, user.languageCode))
    }

    fun launch(key: String, task: suspend () -> Unit): Boolean {
        if (requestsMap.containsKey(key))
            if (requestsMap[key]?.isCompleted == false)
                return false

        log.debug { "launch --> $key" }

        requestsMap[key] =
            scope.launch {
                try {
                    task()
                } catch (e: Exception) {
                    log.error { "job --> $key: ${e.message}" }
                }
            }

        return true
    }

    fun cancelJobs(ctx: MessageContext) {

        fun replyToMessage(text: String) = ctx.replyToMessage(text).setParseMode(ParseMode.HTML).callAsync(ctx.sender)

        val userId = ctx.user().id
        val languageCode = ctx.user().languageCode
        val activeJobs = requestsMap.filter { it.key.endsWith("@$userId") && it.value.isActive }

        if (activeJobs.isEmpty()) {
            replyToMessage(Strings.localizedString(Strings.NO_ACTIVE_TASKS, languageCode))
            return
        }

        activeJobs.forEach { (key, job) ->
            log.debug { "job.cancel --> $key" }
            job.cancel()
        }

        replyToMessage(Strings.localizedString(Strings.TASKS_ARE_CANCELLED, languageCode))
    }
}
