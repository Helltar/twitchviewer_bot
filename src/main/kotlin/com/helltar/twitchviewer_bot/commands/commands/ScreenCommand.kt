package com.helltar.twitchviewer_bot.commands.commands

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import com.helltar.twitchviewer_bot.BotConfig
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.commands.TwitchCommand
import com.helltar.twitchviewer_bot.utils.Utils
import org.slf4j.LoggerFactory
import java.io.File

class ScreenCommand(bot: Bot, message: Message, args: List<String> = listOf()) : TwitchCommand(bot, message, args) {

    override fun run() {
        if (args.isEmpty())
            sendMessage(localizedString(Strings.screenshot_command_info))
        else
            getScreenshot(args[0])
    }

    fun getScreenshot(channelName: String) {
        if (checkIsChannelNameValid(channelName))
            if (!twitch.getOnlineList(listOf(channelName)).isNullOrEmpty())
                sendScreenshot(channelName)
            else
                sendMessage(localizedString(Strings.stream_offline))
    }

    private fun sendScreenshot(channelName: String) {
        val filenameKey = "${channelName}_${Utils.randomUUID()}"

        val tempMessageId = sendMessage(String.format(localizedString(Strings.wait_get_screenshot), channelName))
        twitch.getScreenshot(filenameKey, channelName)
        deleteMessage(tempMessageId)

        val filename = String.format(BotConfig.FILE_FFMPEG_OUT_IMAGE, filenameKey)

        if (File(filename).exists()) {
            sendPhoto(File(filename), "#$channelName", message.messageId)

            try {
                File(filename).delete()
            } catch (e: Exception) {
                LoggerFactory.getLogger(javaClass).error(e.message)
            }
        } else
            sendMessage(String.format(localizedString(Strings.get_clip_fail)))
    }
}
