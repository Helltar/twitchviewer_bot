package com.helltar.twitchviewerbot.commands.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.BotConfig.creatorId
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand
import com.helltar.twitchviewerbot.dao.DatabaseFactory.userChannels

class AddCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    private val maxChannelsSize = if (userId != creatorId) 8 else 12

    override fun run() {
        if (ctx.user().isBot)
            return

        if (args.isNotEmpty())
            add(args.first())
        else
            replyToMessage(localizedString(Strings.add_command_info))
    }

    private fun add(channel: String) {
        if (!isChannelNameValid(channel))
            return

        val userChannelsListSize = getUserChannelsList().size

        if (userChannelsListSize < maxChannelsSize) {
            if (addChannelToUserList(channel))
                replyToMessage(String.format(localizedString(Strings.channel_added_to_list), channel))
            else
                replyToMessage(String.format(localizedString(Strings.channel_already_exists_in_list), channel))
        } else
            replyToMessage(localizedString(Strings.list_full))
    }

    private fun addChannelToUserList(channel: String) =
        if (userChannels.isChannelNotExists(userId, channel))
            userChannels.add(userId, channel.lowercase())
        else
            false
}