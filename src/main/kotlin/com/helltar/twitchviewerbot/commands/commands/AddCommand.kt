package com.helltar.twitchviewerbot.commands.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand
import com.helltar.twitchviewerbot.db.Databases.dbUserChannels

class AddCommand(ctx: MessageContext,  args: List<String> = listOf()) : TwitchCommand(ctx, args) {

    override fun run() {
        if (!isBot)
            add(args.ifEmpty {
                replyToMessage(localizedString(Strings.add_command_info))
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
                replyToMessage(String.format(localizedString(Strings.channel_added_to_list), channel))
            else
                replyToMessage(String.format(localizedString(Strings.channel_already_exists_in_list), channel))
        } else
            replyToMessage(localizedString(Strings.list_full))
    }

    private fun addChannelToUserList(channel: String) =
        dbUserChannels.add(userId, channel.lowercase())
}
