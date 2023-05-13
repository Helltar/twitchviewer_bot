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
        twitchStreamsData.forEach { streamData ->
            val tempMessage = String.format(localizedString(Strings.start_get_clip), streamData.username)
            val tempMessageId = replyToMessage(tempMessage)
            val clipFilename = twitch.getShortClip(streamData.login)

            if (!File(clipFilename).exists()) {
                replyToMessage(localizedString(Strings.get_clip_fail))
                deleteMessage(tempMessageId)
                return
            }

            if (!compress) {
                streamData.run {
                    val htmlTitle = "<b><a href=\"https://www.twitch.tv/$login\">$username</a></b> - $title\n\n"
                    val viewers = "\uD83D\uDC40 <b>$viewerCount</b>\n" // 👀
                    val time = String.format(localizedString(Strings.stream_start_time), startedAt, uptime) + "\n\n"
                    val gameName = if (gameName.isNotEmpty()) ", #${Utils.replaceTitleTag(gameName)}" else ""

                    replyToMessageWithVideo(clipFilename, "$htmlTitle$viewers$time#${username}$gameName")
                }

                File(clipFilename).delete()
            } else
                compressAndSendVideo(clipFilename)

            deleteMessage(tempMessageId)
        }
    }
}