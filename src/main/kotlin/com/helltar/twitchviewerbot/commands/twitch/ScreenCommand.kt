package com.helltar.twitchviewerbot.commands.twitch

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Config.javaTempDir
import com.helltar.twitchviewerbot.Extensions.plusUUID
import com.helltar.twitchviewerbot.Extensions.toHashTag
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand
import com.helltar.twitchviewerbot.twitch.Utils.executeStreamlink
import com.helltar.twitchviewerbot.twitch.Utils.ffmpegExtractFrame
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit

class ScreenCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun run() {
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
                replyToMessage(localizedString(Strings.STREAM_OFFLINE).format(channelName))
        }
    }

    private fun sendScreenshot(channel: String, username: String, gameName: String) {
        val tempMessageId = replyToMessage(localizedString(Strings.WAIT_GET_SCREENSHOT).format(channel))

        val tempName = channel.plusUUID()
        val ffmpegOutFilename = "ffmpeg_$tempName.png"
        val streamlinkOutFilename = "streamlink_$tempName.mp4"
        val screenFilename = "$javaTempDir/$ffmpegOutFilename"
        val screenFile = File(screenFilename)

        try {
            val streamlinkProcess = executeStreamlink(30, channel, streamlinkOutFilename)

            if (!streamlinkProcess.waitFor(50, TimeUnit.SECONDS))
                streamlinkProcess.destroy()

            val ffmpegProcess = ffmpegExtractFrame(streamlinkOutFilename, ffmpegOutFilename)

            if (!ffmpegProcess.waitFor(20, TimeUnit.SECONDS))
                ffmpegProcess.destroy()

            if (screenFile.exists()) {
                val url = """<a href="https://www.twitch.tv/$channel">Twitch</a>"""
                val game = if (gameName.isNotEmpty()) ", #${gameName.toHashTag()}" else ""
                replyToMessageWithPhoto(screenFile, "#$username$game - $url")
            } else
                replyToMessage(localizedString(Strings.GET_CLIP_FAIL))
        } catch (e: Exception) {
            log.error("error processing screenshot for $channel: ${e.message}")
        } finally {
            File("$javaTempDir/$streamlinkOutFilename").delete()
            screenFile.delete()
            deleteMessage(tempMessageId)
        }
    }
}
