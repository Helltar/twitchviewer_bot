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
                // todo: liveCmd duplicate
                val username = Utils.escapeHtml(it.username)
                val title = Utils.escapeHtml(it.title)
                val htmlTitle = "<b><a href=\"https://www.twitch.tv/${it.login}\">$username</a></b> - $title\n\n"
                val viewerCount = "\uD83D\uDC64 <b>${it.viewerCount}</b>\n" // 👤
                val time = String.format(localizedString(Strings.stream_start_time), it.startedAt, it.uptime) + "\n\n"
                val gameName = if (it.gameName.isNotEmpty()) ", #${Utils.replaceTitleTag(it.gameName)}" else ""

                replyToMessageWithVideo(filename, "$htmlTitle$viewerCount$time#${it.username}$gameName").messageId

                File(filename).delete()
            } else
                compressAndSendVideo(filename)

            deleteMessage(tempMessageId)
        }
    }
}
