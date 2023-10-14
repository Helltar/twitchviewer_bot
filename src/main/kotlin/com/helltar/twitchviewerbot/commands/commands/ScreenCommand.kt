package com.helltar.twitchviewerbot.commands.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand
import com.helltar.twitchviewerbot.utils.Utils
import java.io.File

class ScreenCommand(ctx: MessageContext, args: List<String> = listOf()) : TwitchCommand(ctx, args) {

    override fun run() {
        if (args.isEmpty())
            replyToMessage(localizedString(Strings.screenshot_command_info))
        else
            getScreenshot(args[0])
    }

    fun getScreenshot(channelName: String) {
        if (checkIsChannelNameValid(channelName)) {
            val streamData = twitch.getOnlineList(listOf(channelName))

            if (!streamData.isNullOrEmpty())
                sendScreenshot(channelName, streamData[0].username, streamData[0].gameName)
            else
                replyToMessage(localizedString(Strings.stream_offline))
        }
    }

    private fun sendScreenshot(channelName: String, username: String, gameName: String) {
        val tempMessageId = replyToMessage(String.format(localizedString(Strings.wait_get_screenshot), channelName))
        val filename = twitch.getScreenshot(channelName)
        deleteMessage(tempMessageId)

        if (File(filename).exists()) {
            val url = "<a href=\"https://www.twitch.tv/$channelName\">Twitch</a>"

            replyToMessageWithPhoto(
                File(filename),
                "#$username${if (gameName.isNotEmpty()) ", #${Utils.replaceTitleTag(gameName)}" else ""} - $url"
            )

            File(filename).delete()
        } else
            replyToMessage(String.format(localizedString(Strings.get_clip_fail)))
    }
}
