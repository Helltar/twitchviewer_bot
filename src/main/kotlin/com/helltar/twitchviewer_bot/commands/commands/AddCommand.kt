package com.helltar.twitchviewer_bot.commands.commands

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import com.helltar.twitchviewer_bot.BotConfig.DIR_DB_USER_LIST
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.commands.TwitchCommand
import com.helltar.twitchviewer_bot.utils.Utils
import java.io.File

class AddCommand(bot: Bot, message: Message, args: List<String>) : TwitchCommand(bot, message, args) {

    private val userListFilename = DIR_DB_USER_LIST + userId

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

        if (Utils.getListFromFile(userListFilename).size <= 4) {
            if (addChannelToUserList(channel))
                sendMessage(String.format(localizedString(Strings.channel_added_to_list), channel))
            else
                sendMessage(String.format(localizedString(Strings.channel_already_exists_in_list), channel))
        } else
            sendMessage(localizedString(Strings.list_full))
    }

    private fun addChannelToUserList(channel: String): Boolean {
        val list = arrayListOf<String>()

        if (File(userListFilename).exists())
            list.addAll(Utils.getListFromFile(userListFilename))

        return if (!list.contains(channel)) {
            list.add(channel)
            Utils.addLineToFile(userListFilename, list[list.lastIndex])
            true
        } else
            false
    }
}
