package com.helltar.twitchviewer_bot.commands.commands

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.commands.TwitchCommand
import com.helltar.twitchviewer_bot.db.Databases.dbUserChannels

class AddCommand(bot: Bot, message: Message, args: List<String>) : TwitchCommand(bot, message, args) {

    override fun run() {
        if (!isBot)
            add(args.ifEmpty {
                sendMessage(localizedString(Strings.add_command_info))
                return
            })
        else
            super.run()
    }

    private fun add(args: List<String>) {
        if (!checkIsChannelNameValid())
            return

        val channel = args[0]
        val listSize = dbUserChannels.getSize(userId)

        if (listSize <= 4) {
            if (addChannelToUserList(channel))
                sendMessage(String.format(localizedString(Strings.channel_added_to_list), channel))
            else
                sendMessage(String.format(localizedString(Strings.channel_already_exists_in_list), channel))
        } else
            sendMessage(localizedString(Strings.list_full))
    }

    private fun addChannelToUserList(channel: String) =
        dbUserChannels.add(userId, channel.lowercase())
}
