package com.helltar.twitchviewer_bot.twitch

import com.github.twitch4j.TwitchClientBuilder
import com.helltar.twitchviewer_bot.BotConfig.SH_FFMPEG_SCREENSHOT
import com.helltar.twitchviewer_bot.BotConfig.SH_YTDLP_FFMPEG
import com.helltar.twitchviewer_bot.BotConfig.TWITCH_TOKEN
import org.slf4j.LoggerFactory
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

    /* todo: runProcess fun. */

    fun getShortClip(filenameKey: String, channelName: String) =
        try {
            ProcessBuilder(SH_YTDLP_FFMPEG, channelName, filenameKey).start().waitFor()
        } catch (e: Exception) {
            log.error(e.message)
        }

    fun getScreenshot(filenameKey: String, channelName: String) =
        try {
            ProcessBuilder(SH_FFMPEG_SCREENSHOT, channelName, filenameKey).start().waitFor()
        } catch (e: Exception) {
            log.error(e.message)
        }
}
