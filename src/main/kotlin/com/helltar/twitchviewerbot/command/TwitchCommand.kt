package com.helltar.twitchviewerbot.command

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.dao.DatabaseFactory.userChannelsTable
import com.helltar.twitchviewerbot.twitch.Twitch
import java.util.*

abstract class TwitchCommand(ctx: MessageContext) : BotCommand(ctx) {

    protected val twitch = Twitch()

    protected fun getUserChannelsList(userId: Long = this.userId) =
        userChannelsTable.getUserChannelsList(userId)

    protected fun isUserListEmpty(userId: Long = this.userId) =
        userChannelsTable.isUserListEmpty(userId)

    protected fun isUserListNotEmpty(userId: Long = this.userId) =
        userChannelsTable.isUserListNotEmpty(userId)

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
        return systemTimeZone.rawOffset / (1000 * 60 * 60)
    }
}