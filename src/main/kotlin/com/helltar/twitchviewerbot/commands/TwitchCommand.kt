package com.helltar.twitchviewerbot.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.dao.DatabaseFactory.userChannels
import com.helltar.twitchviewerbot.twitch.Twitch

abstract class TwitchCommand(ctx: MessageContext) : BotCommand(ctx) {

    protected val twitch = Twitch()

    protected fun getUserChannelsList(userId: Long = this.userId) =
        userChannels.getUserChannelsList(userId)

    protected fun isUserListEmpty(userId: Long = this.userId) =
        userChannels.isUserListEmpty(userId)

    protected fun isUserListNotEmpty(userId: Long = this.userId) =
        userChannels.isUserListNotEmpty(userId)

    protected fun isChannelNameValid(channelName: String): Boolean {
        if (channelName.length !in 2..25) {
            replyToMessage(localizedString(Strings.invalid_channel_name_length))
            return false
        }

        if (!channelName.matches("^[a-zA-Z0-9_]*$".toRegex())) {
            replyToMessage(localizedString(Strings.invalid_channel_name))
            return false
        }

        return true
    }
}