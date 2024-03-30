package com.helltar.twitchviewerbot.command.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Extensions.toHashTag
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.command.TwitchCommand
import com.helltar.twitchviewerbot.twitch.TwitchUtils
import java.io.File

class ScreenCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    override fun run() {
        if (arguments.isEmpty())
            replyToMessage(localizedString(Strings.SCREENSHOT_COMMAND_INFO))
        else
            getScreenshot(arguments.first())
    }

    fun getScreenshot(channelName: String) {
        if (isChannelNameValid(channelName)) {
            val streamData = twitch.getOnlineList(listOf(channelName))

            if (!streamData.isNullOrEmpty())
                sendScreenshot(channelName, streamData.first().username, streamData.first().gameName)
            else
                replyToMessage(localizedString(Strings.STREAM_OFFLINE.format(channelName)))
        }
    }

    private fun sendScreenshot(channel: String, username: String, gameName: String) {
        val tempMessageId = replyToMessage(localizedString(Strings.WAIT_GET_SCREENSHOT.format(channel)))
        val filename = TwitchUtils.getScreenshot(channel)
        deleteMessage(tempMessageId)

        if (File(filename).exists()) {
            val url = """<a href="https://www.twitch.tv/$channel">Twitch</a>"""
            val game = if (gameName.isNotEmpty()) ", #${gameName.toHashTag()}" else ""

            replyToMessageWithPhoto(
                File(filename),
                "#$username$game - $url"
            )

            File(filename).delete()
        } else
            replyToMessage(localizedString(Strings.GET_CLIP_FAIL))
    }
}