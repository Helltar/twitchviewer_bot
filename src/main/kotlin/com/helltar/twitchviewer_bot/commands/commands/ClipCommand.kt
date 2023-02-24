package com.helltar.twitchviewer_bot.commands.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.twitch.Twitch
import com.helltar.twitchviewer_bot.utils.Utils
import java.io.File

class ClipCommand(ctx: MessageContext, args: List<String> = listOf()) : ClipCompressCommand(ctx, args) {

    override fun run() {
        if (args.isEmpty())
            if (isUserListNotEmpty())
                getClipsFromAll(getUserChannelsList())
            else
                replyToMessage(localizedString(Strings.clip_command_info))
        else
            if (checkIsChannelNameValid()) {
                val compress = args.size > 1 && args[1].startsWith(".")
                getClipsFromAll(listOf(args[0]), compress)
            }
    }

    fun getClipsFromAll(userLogins: List<String>, compress: Boolean = false) {
        twitch.getOnlineList(userLogins)?.let {
            if (it.isNotEmpty())
                sendClip(it, compress)
            else {
                val text = if (userLogins.size > 1)
                    localizedString(Strings.empty_online_list)
                else
                    localizedString(Strings.stream_offline)

                replyToMessage(text)
            }
        }
            ?: replyToMessage(localizedString(Strings.twitch_exception))
    }

    private fun sendClip(twitchStreamsData: List<Twitch.StreamsData>, compress: Boolean) {
        twitchStreamsData.forEach {
            val tempMessageId = replyToMessage(String.format(localizedString(Strings.start_get_clip), it.username))
            val filename = twitch.getShortClip(it.login)

            if (!File(filename).exists()) {
                replyToMessage(localizedString(Strings.get_clip_fail))
                deleteMessage(tempMessageId)
                return
            }

            if (!compress) {
                val gameName = if (it.gameName.isNotEmpty())
                    ", #${Utils.replaceTitleTag(it.gameName)}"
                else ""

                val url = "<a href=\"https://www.twitch.tv/${it.login}\">Twitch</a>"
                replyToMessageWithVideo(filename, "#${it.username}$gameName - $url").messageId

                File(filename).delete()
            } else
                compressAndSendVideo(filename)

            deleteMessage(tempMessageId)
        }
    }
}
