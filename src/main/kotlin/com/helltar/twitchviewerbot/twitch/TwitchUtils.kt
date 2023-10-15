package com.helltar.twitchviewerbot.twitch

import com.helltar.twitchviewerbot.BotConfig.DIR_TEMP
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

object TwitchUtils {

    private val log = LoggerFactory.getLogger(javaClass)

    fun getShortClip(channel: String): String {
        val tempName = genRandomName(channel)
        val ffmpegOutFilename = "ffmpeg_$tempName.mp4"
        val streamlinkOutFilename = "streamlink_$tempName.mp4"

        executeStreamlink(35, channel, streamlinkOutFilename)

        "timeout -k 5 -s SIGINT 60 ffmpeg -i $streamlinkOutFilename -c copy -loglevel quiet $ffmpegOutFilename".startProcess()

        File("$DIR_TEMP/$streamlinkOutFilename").delete()

        return "$DIR_TEMP/$ffmpegOutFilename"
    }

    fun getScreenshot(channel: String): String {
        val tempName = genRandomName(channel)
        val ffmpegOutFilename = "ffmpeg_$tempName.png"
        val streamlinkOutFilename = "streamlink_$tempName.mp4"

        executeStreamlink(30, channel, streamlinkOutFilename)

        "timeout -k 5 15 ffmpeg -ss 00:00:05 -i $streamlinkOutFilename -vframes 1 $ffmpegOutFilename".startProcess()

        File("$DIR_TEMP/$streamlinkOutFilename").delete()

        return "$DIR_TEMP/$ffmpegOutFilename"
    }

    private fun executeStreamlink(stopTimeout: Int, channel: String, outFilename: String) =
        "timeout -k 10 -s SIGINT $stopTimeout streamlink --twitch-disable-ads https://www.twitch.tv/$channel 720p60,best -o $outFilename".startProcess()

    private fun genRandomName(name: String) =
        "${name}_${UUID.randomUUID()}"

    private fun String.startProcess() {
        try {
            val file = File(DIR_TEMP)

            if (!file.exists()) {
                if (!file.mkdir()) return
            } else if (!file.isDirectory) return

            ProcessBuilder(this.split(" ")).directory(file).start().waitFor()
        } catch (e: Exception) {
            log.error(e.message, e)
        }
    }
}