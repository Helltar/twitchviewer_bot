package com.helltar.twitchviewerbot.twitch

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.helltar.twitchviewerbot.EnvConfig.twitchToken
import com.helltar.twitchviewerbot.Extensions.escapeHtml
import org.slf4j.LoggerFactory
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Twitch {

    private companion object {
        val twitchClient: TwitchClient = TwitchClientBuilder.builder().withEnableHelix(true).build()
    }

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
        twitchClient
            .helix
            .getStreams(twitchToken, null, null, 1, null, null, null, userLogins)
            .execute()
            .streams
            .map {
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val startedAt = it.startedAtInstant.atZone(ZoneId.systemDefault()).format(formatter)
                val thumbnailUrl = it.getThumbnailUrl(1920, 1080)
                val uptime = LocalTime.MIN.plus(it.uptime).format(formatter)

                BroadcastData(
                    it.userLogin, it.userName.escapeHtml(), it.title.escapeHtml(),
                    it.viewerCount, it.gameName, thumbnailUrl, startedAt, uptime
                )
            }
    } catch (e: Exception) {
        log.error(e.message)
        null
    }
}