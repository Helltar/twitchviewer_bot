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

    data class StreamsData(
        val login: String,
        val username: String,
        val title: String,
        val viewerCount: Int,
        val gameName: String,
        val thumbnailUrl: String,
        val startedAt: String,
        val uptime: String
    )

    fun getOnlineList(userLogins: List<String>): List<StreamsData>? {
        try {
            val list = arrayListOf<StreamsData>()

            TwitchClientBuilder.builder()
                .withEnableHelix(true).build()
                // todo: helix.getStreams (limit)
                .helix.getStreams(TWITCH_TOKEN, null, null, 1, null, null, null, userLogins)
                .execute().streams.forEach {

                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    val startedAt = it.startedAtInstant.atZone(ZoneId.systemDefault()).format(formatter)
                    val thumbnailUrl = it.getThumbnailUrl(1920, 1080)
                    val uptime = LocalTime.MIN.plus(it.uptime).format(formatter)

                    list.add(StreamsData(it.userLogin, it.userName, it.title, it.viewerCount, it.gameName, thumbnailUrl, startedAt, uptime))
                }

            return list
        } catch (e: Exception) {
            log.error(e.message)
            return null
        }
    }

    fun getShortClip(channelName: String): String {
        val tempName = genRandomName(channelName)
        val ffmpegOutFilename = "ffmpeg_$tempName.mp4"
        val ytDlpOutFilename = genYtDlpTempFilename(tempName)

        runYtDlp(35, channelName, ytDlpOutFilename)

        runProcess(
            "timeout -k 5 -s SIGINT 60 ffmpeg -ss 00:00:15 -i $ytDlpOutFilename -c copy -loglevel quiet $ffmpegOutFilename",
            DIR_TEMP
        )

        File(DIR_TEMP + ytDlpOutFilename).delete()

        return DIR_TEMP + ffmpegOutFilename
    }

    fun getScreenshot(channelName: String): String {
        val tempName = genRandomName(channelName)
        val ffmpegOutFilename = "ffmpeg_$tempName.png"
        val ytDlpOutFilename = genYtDlpTempFilename(tempName)

        runYtDlp(25, channelName, ytDlpOutFilename)
        runProcess("timeout -k 5 15 ffmpeg -ss 00:00:17 -i $ytDlpOutFilename -vframes 1 $ffmpegOutFilename", DIR_TEMP)

        File(DIR_TEMP + ytDlpOutFilename).delete()

        return DIR_TEMP + ffmpegOutFilename
    }

    private fun runYtDlp(timeout: Int, channelName: String, outFilename: String) =
        runProcess("timeout -k 10 -s SIGINT $timeout yt-dlp https://www.twitch.tv/$channelName -q -o $outFilename", DIR_TEMP)

    private fun genYtDlpTempFilename(name: String) = "yt_dlp_$name.mp4"

    private fun genRandomName(name: String) = "${name}_${Utils.randomUUID()}"
}
