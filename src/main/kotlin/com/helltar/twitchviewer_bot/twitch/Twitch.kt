package com.helltar.twitchviewer_bot.twitch

import com.github.twitch4j.TwitchClientBuilder
import com.helltar.twitchviewer_bot.BotConfig.DIR_TEMP
import com.helltar.twitchviewer_bot.BotConfig.TWITCH_TOKEN
import com.helltar.twitchviewer_bot.utils.Utils
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

    fun getShortClip(channelName: String) =
        try {
            val tempName = "${channelName}_${Utils.randomUUID()}"
            val ffmpegOutFilename = "ffmpeg_$tempName.mp4"
            val ytDlpOutFilename = "yt_dlp_$tempName.mp4"

            runProcess("timeout -k 10 -s SIGINT 35 yt-dlp https://www.twitch.tv/$channelName -q -o $ytDlpOutFilename")
            runProcess("timeout -k 5 -s SIGINT 60 ffmpeg -ss 00:00:16 -i $ytDlpOutFilename -c copy -loglevel quiet $ffmpegOutFilename")

            File(DIR_TEMP + ytDlpOutFilename).delete()

            DIR_TEMP + ffmpegOutFilename
        } catch (e: Exception) {
            log.error(e.message)
            ""
        }

    fun getScreenshot(channelName: String) =
        try {
            val tempName = "${channelName}_${Utils.randomUUID()}"
            val ffmpegOutFilename = "ffmpeg_$tempName.jpg"
            val ytDlpOutFilename = "yt_dlp_$tempName.mp4"

            runProcess("timeout -k 10 -s SIGINT 25 yt-dlp https://www.twitch.tv/$channelName -q -o $ytDlpOutFilename")
            runProcess("timeout -k 5 15 ffmpeg -ss 00:00:17 -i $ytDlpOutFilename -vframes 1 $ffmpegOutFilename")

            File(DIR_TEMP + ytDlpOutFilename).delete()

            DIR_TEMP + ffmpegOutFilename
        } catch (e: Exception) {
            log.error(e.message)
            ""
        }

    private fun runProcess(command: String, workDir: String = DIR_TEMP) {
        val file = File(workDir)

        if (!file.exists()) {
            if (!file.mkdir()) return
        } else if (!file.isDirectory) return

        ProcessBuilder(command.split(" ")).directory(file).start().waitFor()
    }
}
