package com.helltar.twitchviewerbot.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.db.dao.userChannelsDao
import com.helltar.twitchviewerbot.twitch.Twitch

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
}
