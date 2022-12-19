package com.helltar.twitchviewer_bot.commands.commands

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.TelegramFile
import com.helltar.twitchviewer_bot.BotConfig
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.commands.TwitchCommand
import com.helltar.twitchviewer_bot.twitch.Twitch
import com.helltar.twitchviewer_bot.utils.Utils
import org.slf4j.LoggerFactory
import java.io.File

class ClipCommand(bot: Bot, message: Message, args: List<String> = listOf()) : TwitchCommand(bot, message, args) {

    override fun run() {
        if (args.isEmpty())
            if (isUserListNotEmpty())
                getClipsFromAll(getUserChannelsList())
            else
                sendMessage(localizedString(Strings.clip_command_info))
        else
            if (checkIsChannelNameValid())
                getClipsFromAll(listOf(args[0]))
    }

    fun getClipsFromAll(userLogins: List<String>) {
        twitch.getOnlineList(userLogins)?.let {
            if (it.isNotEmpty())
                sendClip(it)
            else {
                val text = if (userLogins.size > 1)
                    localizedString(Strings.empty_online_list)
                else
                    localizedString(Strings.stream_offline)

                sendMessage(text)
            }

            return
        }

        sendMessage(localizedString(Strings.twitch_exception))
    }

    private fun sendClip(twitchStreamsData: List<Twitch.StreamsData>) {
        twitchStreamsData.forEach {
            val tempMessageId = sendMessage(String.format(localizedString(Strings.start_get_clip), it.username))
            val filename = twitch.getShortClip(it.login)
            deleteMessage(tempMessageId)

            if (!File(filename).exists()) {
                sendMessage(localizedString(Strings.get_clip_fail))
                return
            }

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

            try {
                File(filename).delete()
            } catch (e: Exception) {
                LoggerFactory.getLogger(javaClass)
            }
        }
    }
}
