package com.helltar.twitchviewerbot.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.db.Databases.dbUserChannels
import com.helltar.twitchviewerbot.twitch.Twitch

open class TwitchCommand(ctx: MessageContext, args: List<String> = listOf()) : BotCommand(ctx, args) {

    protected val twitch = Twitch()
    protected var isBot = ctx.user().isBot

    override fun run() {
        replyToMessage("Hi, <b>Anonymous</b> \uD83C\uDF1A") // 🌚
    }

    fun getUserChannelsList(userId: Long = this.userId) =
        dbUserChannels.getList(userId)

    protected fun isUserListNotEmpty() =
        dbUserChannels.isNotEmpty(userId)

    protected fun checkIsChannelNameValid(channelName: String = if (args.isNotEmpty()) args[0] else ""): Boolean {
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