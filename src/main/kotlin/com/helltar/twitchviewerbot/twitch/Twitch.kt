package com.helltar.twitchviewerbot.twitch

import com.github.twitch4j.TwitchClientBuilder
import com.helltar.twitchviewerbot.EnvConfig.twitchToken
import com.helltar.twitchviewerbot.Extensions.escapeHtml
import org.slf4j.LoggerFactory
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Twitch {

    private val client = TwitchClientBuilder.builder().withEnableHelix(true).build()
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

    fun getOnlineList(userLogins: List<String>) = try {
        val streamsList =
            client
                .helix
                .getStreams(twitchToken, null, null, 1, null, null, null, userLogins)
                .execute()
                .streams

        arrayListOf<BroadcastData>().apply {
            streamsList.forEach { broadcast ->
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val startedAt = broadcast.startedAtInstant.atZone(ZoneId.systemDefault()).format(formatter)
                val thumbnailUrl = broadcast.getThumbnailUrl(1920, 1080)
                val uptime = LocalTime.MIN.plus(broadcast.uptime).format(formatter)

                add(
                    BroadcastData(
                        broadcast.userLogin, broadcast.userName.escapeHtml(), broadcast.title.escapeHtml(),
                        broadcast.viewerCount, broadcast.gameName, thumbnailUrl, startedAt, uptime
                    )
                )
            }
        }
    } catch (e: Exception) {
        log.error(e.message)
        null
    }
}