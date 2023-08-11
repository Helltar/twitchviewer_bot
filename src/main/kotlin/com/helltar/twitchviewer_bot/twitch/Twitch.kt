package com.helltar.twitchviewer_bot.twitch

import com.github.twitch4j.TwitchClientBuilder
import com.helltar.twitchviewer_bot.BotConfig.DIR_TEMP
import com.helltar.twitchviewer_bot.BotConfig.TWITCH_TOKEN
import com.helltar.twitchviewer_bot.utils.Utils
import com.helltar.twitchviewer_bot.utils.Utils.runProcess
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Twitch {

    private val log = LoggerFactory.getLogger(javaClass)

    data class BroadcastData(
        val login: String,
        val username: String,
        val title: String,
        val viewerCount: Int,
        val gameName: String,
        val thumbnailUrl: String,
        val startedAt: String,
        val uptime: String
    )

    fun getOnlineList(userLogins: List<String>): List<BroadcastData>? {
        val broadcasts = arrayListOf<BroadcastData>()
        val twitchClient = TwitchClientBuilder.builder().withEnableHelix(true).build()

        return try {
            val streams = twitchClient.helix.getStreams(TWITCH_TOKEN, null, null, 1, null, null, null, userLogins).execute().streams

            streams.forEach { stream ->
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val startedAt = stream.startedAtInstant.atZone(ZoneId.systemDefault()).format(formatter)
                val thumbnailUrl = stream.getThumbnailUrl(1920, 1080)
                val uptime = LocalTime.MIN.plus(stream.uptime).format(formatter)

                broadcasts.add(
                    BroadcastData(
                        stream.userLogin, Utils.escapeHtml(stream.userName), Utils.escapeHtml(stream.title),
                        stream.viewerCount, stream.gameName, thumbnailUrl, startedAt, uptime
                    )
                )
            }

            broadcasts
        } catch (e: Exception) {
            log.error(e.message)
            null
        }
    }

    fun getShortClip(channelName: String): String {
        val tempName = genRandomName(channelName)
        val ffmpegOutFilename = "ffmpeg_$tempName.mp4"
        val streamlinkOutFilename = "streamlink_$tempName.mp4"

        runStreamlink(35, channelName, streamlinkOutFilename)
        runProcess("timeout -k 5 -s SIGINT 60 ffmpeg -i $streamlinkOutFilename -c copy -loglevel quiet $ffmpegOutFilename", DIR_TEMP)

        File(DIR_TEMP + streamlinkOutFilename).delete()

        return DIR_TEMP + ffmpegOutFilename
    }

    fun getScreenshot(channelName: String): String {
        val tempName = genRandomName(channelName)
        val ffmpegOutFilename = "ffmpeg_$tempName.png"
        val streamlinkOutFilename = "streamlink_$tempName.mp4"

        // todo: streamlink. screenshot timeout
        runStreamlink(10, channelName, streamlinkOutFilename)
        runProcess("timeout -k 5 15 ffmpeg -ss 00:00:05 -i $streamlinkOutFilename -vframes 1 $ffmpegOutFilename", DIR_TEMP)

        File(DIR_TEMP + streamlinkOutFilename).delete()

        return DIR_TEMP + ffmpegOutFilename
    }

    private fun runStreamlink(timeout: Int, channelName: String, outFilename: String) =
        runProcess(
            "timeout -k 10 -s SIGINT $timeout streamlink --twitch-disable-ads https://www.twitch.tv/$channelName 720p60,best -o $outFilename",
            DIR_TEMP
        )

    private fun genRandomName(name: String) =
        "${name}_${Utils.randomUUID()}"
}