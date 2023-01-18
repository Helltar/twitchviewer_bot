package com.helltar.twitchviewer_bot.commands.commands

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.TelegramFile
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.twitch.Twitch
import com.helltar.twitchviewer_bot.utils.Utils
import java.io.File

class ClipCommand(bot: Bot, message: Message, args: List<String> = listOf()) : ClipCompressCommand(bot, message, args) {

    override fun run() {
        if (args.isEmpty())
            if (isUserListNotEmpty())
                getClipsFromAll(getUserChannelsList())
            else
                sendMessage(localizedString(Strings.clip_command_info))
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

                sendMessage(text)
            }
        }
            ?: sendMessage(localizedString(Strings.twitch_exception))
    }

    private fun sendClip(twitchStreamsData: List<Twitch.StreamsData>, compress: Boolean) {
        twitchStreamsData.forEach {
            val tempMessageId = sendMessage(String.format(localizedString(Strings.start_get_clip), it.username))
            val filename = twitch.getShortClip(it.login)
            deleteMessage(tempMessageId)

            if (!File(filename).exists()) {
                sendMessage(localizedString(Strings.get_clip_fail))
                return
            }

            if (!compress) {
                val gameName = if (it.gameName.isNotEmpty())
                    ", #${Utils.replaceTitleTag(it.gameName)}"
                else ""

                bot.sendVideo(
                    chatId,
                    TelegramFile.ByFile(File(filename)),
                    15, 1920, 1080, // -c copy
                    caption = "#${it.username}$gameName",
                    replyToMessageId = replyToMessageId, allowSendingWithoutReply = true
                )

                File(filename).delete()
            } else
                compressAndSendVideo(filename)
        }
    }
}
