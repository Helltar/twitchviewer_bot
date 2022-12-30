package com.helltar.twitchviewer_bot.commands

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.db.Databases.dbUserChannels
import com.helltar.twitchviewer_bot.twitch.Twitch

open class TwitchCommand(bot: Bot, message: Message, args: List<String> = listOf()) : BotCommand(bot, message, args) {

    protected val twitch = Twitch()
    protected var isBot = message.from!!.isBot

    override fun run() {
        sendMessage("Hi, <b>Anonymous</b> \uD83C\uDF1A") // 🌚
    }

    fun getUserChannelsList(userId: Long = this.userId) =
        dbUserChannels.getList(userId)

    protected fun isUserListNotEmpty() =
        dbUserChannels.isNotEmpty(userId)

    protected fun checkIsChannelNameValid(channelName: String = args[0] /* todo: args.empty! */): Boolean {
        if (channelName.length !in 2..25) {
            sendMessage(localizedString(Strings.invalid_channel_name_length))
            return false
        }

        if (!channelName.matches("^[a-zA-Z0-9_]*$".toRegex())) {
            sendMessage(localizedString(Strings.invalid_channel_name))
            return false
        }

        return true
    }
}
