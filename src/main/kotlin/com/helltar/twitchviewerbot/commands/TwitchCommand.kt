package com.helltar.twitchviewerbot.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.database.dao.userChannelsDao
import com.helltar.twitchviewerbot.twitch.Twitch
import com.helltar.twitchviewerbot.twitch.Utils.createTwitchHtmlLink
import com.helltar.twitchviewerbot.twitch.Utils.toHashTag

abstract class TwitchCommand(ctx: MessageContext) : BotCommand(ctx) {

    protected val twitch = Twitch()

    protected suspend fun getUserChannelsList(userId: Long = this.userId) =
        userChannelsDao.getChannels(userId)

    protected suspend fun isUserListNotEmpty(userId: Long = this.userId) =
        userChannelsDao.isChannelsListNotEmpty(userId)

    protected fun checkChannelNameAndReplyIfInvalid(name: String): Boolean {
        if (name.length !in 2..25) {
            replyToMessage(localizedString(Strings.INVALID_CHANNEL_NAME_LENGTH))
            return false
        }

        if (!name.matches("^[a-zA-Z0-9_]*$".toRegex())) {
            replyToMessage(localizedString(Strings.INVALID_CHANNEL_NAME))
            return false
        }

        return true
    }

    protected fun createHtmlCaption(broadcastData: Twitch.BroadcastData): String {
        val username = broadcastData.username
        val category = broadcastData.gameName

        val title = "${createTwitchHtmlLink(broadcastData.login, username)} - ${broadcastData.title}\n\n"
        val categoryTag = if (category.isNotEmpty()) ", #${category.toHashTag()}" else ""
        val startTime = localizedString(Strings.STREAM_START_TIME).format(broadcastData.uptime) + "\n\n"
        val viewersCount = localizedString(Strings.STREAM_VIEWERS).format(broadcastData.viewerCount) + "\n"

        return "$title$viewersCount$startTime#$username$categoryTag"
    }
}
