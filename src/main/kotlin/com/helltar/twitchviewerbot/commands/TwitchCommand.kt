package com.helltar.twitchviewerbot.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.db.dao.userChannelsDao
import com.helltar.twitchviewerbot.twitch.Twitch
import java.util.*

abstract class TwitchCommand(ctx: MessageContext) : BotCommand(ctx) {

    protected val twitch = Twitch()

    protected suspend fun getUserChannelsList(userId: Long = this.userId) =
        userChannelsDao.getChannels(userId)

    protected suspend fun isUserListEmpty(userId: Long = this.userId) =
        userChannelsDao.isChannelsListEmpty(userId)

    protected suspend fun isUserListNotEmpty(userId: Long = this.userId) =
        userChannelsDao.isChannelsListNotEmpty(userId)

    protected fun isChannelNameValid(channelName: String): Boolean {
        if (channelName.length !in 2..25) {
            replyToMessage(localizedString(Strings.INVALID_CHANNEL_NAME_LENGTH))
            return false
        }

        if (!channelName.matches("^[a-zA-Z0-9_]*$".toRegex())) {
            replyToMessage(localizedString(Strings.INVALID_CHANNEL_NAME))
            return false
        }

        return true
    }

    protected fun getTimeZoneOffset(): Int {
        val systemTimeZone = TimeZone.getDefault()
        return (systemTimeZone.rawOffset / (1000 * 60 * 60))
    }
}