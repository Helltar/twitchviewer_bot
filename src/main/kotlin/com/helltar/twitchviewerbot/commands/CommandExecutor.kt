package com.helltar.twitchviewerbot.commands

import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.BUTTON_CLIPS
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.BUTTON_LIVE
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.BUTTON_SCREENSHOT
import com.helltar.twitchviewerbot.db.dao.usersDao
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class CommandExecutor {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val requestsMap = ConcurrentHashMap<String, Job>()

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val COMMAND_ADD = "cmdAdd"
        const val COMMAND_LIST = "cmdList"
        const val COMMAND_START = "cmdStart"
        const val COMMAND_ABOUT = "cmdAbout"
        const val COMMAND_PRIVACY = "cmdPrivacy"
        const val COMMAND_CLIP = BUTTON_CLIPS
        const val COMMAND_LIVE = BUTTON_LIVE
        const val COMMAND_SCREENSHOT = BUTTON_SCREENSHOT
    }

    fun execute(botCommand: BotCommand, requestKey: String) {
        val user = botCommand.ctx.user()
        val userId = user.id
        val chat = botCommand.ctx.message().chat
        val commandName = botCommand.javaClass.simpleName

        log.info("$commandName: ${chat.id} $userId ${user.userName} ${user.firstName} ${chat.title}: ${botCommand.ctx.message().text}")

        val launch =
            launch("$requestKey@$userId") {
                if (!usersDao.add(user))
                    usersDao.update(user)

                botCommand.run()
            }

        if (!launch)
            botCommand.replyToMessage(Strings.localizedString(Strings.MANY_REQUEST, user.languageCode))
    }

    fun launch(key: String, block: suspend () -> Unit): Boolean {
        if (requestsMap.containsKey(key))
            if (requestsMap[key]?.isCompleted == false)
                return false

        log.debug("launch --> $key")

        requestsMap[key] = scope.launch { block() }

        return true
    }
}