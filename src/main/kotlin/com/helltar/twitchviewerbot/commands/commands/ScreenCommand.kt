package com.helltar.twitchviewerbot.commands.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand
import com.helltar.twitchviewerbot.utils.Utils
import java.io.File

class ScreenCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    override fun run() {
        if (args.isEmpty())
            replyToMessage(localizedString(Strings.screenshot_command_info))
        else
            getScreenshot(args.first())
    }

    fun getScreenshot(channelName: String) {
        if (isChannelNameValid(channelName)) {
            val streamData = twitch.getOnlineList(listOf(channelName))

            if (!streamData.isNullOrEmpty())
                sendScreenshot(channelName, streamData.first().username, streamData.first().gameName)
            else
                replyToMessage(localizedString(Strings.stream_offline))
        }
    }

    private fun sendScreenshot(channel: String, username: String, gameName: String) {
        val tempMessageId = replyToMessage(String.format(localizedString(Strings.wait_get_screenshot), channel))
        val filename = twitch.getScreenshot(channel)

        deleteMessage(tempMessageId)

        if (File(filename).exists()) {
            val url = "<a href=\"https://www.twitch.tv/$channel\">Twitch</a>"
            val game = if (gameName.isNotEmpty()) ", #${Utils.replaceTitleTag(gameName)}" else ""

            replyToMessageWithPhoto(
                File(filename),
                "#$username$game - $url"
            )

            File(filename).delete()
        } else
            replyToMessage(String.format(localizedString(Strings.get_clip_fail)))
    }
}