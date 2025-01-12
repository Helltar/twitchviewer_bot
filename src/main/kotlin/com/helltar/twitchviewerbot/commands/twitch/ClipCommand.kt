package com.helltar.twitchviewerbot.commands.twitch

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Extensions.toHashTag
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand
import com.helltar.twitchviewerbot.twitch.Twitch
import com.helltar.twitchviewerbot.twitch.Utils
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File

class ClipCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    companion object {
        private const val MAX_SIMULTANEOUS_CLIP_DOWNLOADS = 3
    }

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun run() {
        if (arguments.isEmpty()) {
            if (isUserListNotEmpty())
                getClipsFromAll(getUserChannelsList())
            else
                replyToMessage(localizedString(Strings.CLIP_COMMAND_INFO))
        } else {
            val channel = arguments.first()

            if (isChannelNameValid(channel))
                getClip(channel)
        }
    }

    suspend fun getClipsFromAll(userLogins: List<String>) {
        twitch.getOnlineList(userLogins)?.let {
            if (it.isNotEmpty())
                sendClips(it)
            else {
                val text = if (userLogins.size > 1)
                    localizedString(Strings.EMPTY_ONLINE_LIST)
                else
                    localizedString(Strings.STREAM_OFFLINE).format(userLogins.first())

                replyToMessage(text)
            }
        }
            ?: replyToMessage(localizedString(Strings.TWITCH_EXCEPTION))
    }

    suspend fun getClip(channel: String) =
        getClipsFromAll(listOf(channel))

    private suspend fun sendClips(twitchBroadcastData: List<Twitch.BroadcastData>) = coroutineScope {
        twitchBroadcastData.chunked(MAX_SIMULTANEOUS_CLIP_DOWNLOADS).forEach { chunk ->
            val tempMessage = localizedString(Strings.START_GET_CLIP).format(chunk.joinToString { """<a href="https://www.twitch.tv/${it.login}">${it.username}</a>""" })
            val tempMessageId = replyToMessage(tempMessage)

            try {
                chunk.map { broadcastData ->
                    launch {
                        val channelLogin = broadcastData.login

                        try {
                            val clipFilename = Utils.getShortClip(channelLogin)

                            if (!File(clipFilename).exists()) {
                                replyToMessage(localizedString(Strings.GET_CLIP_FAIL))
                                return@launch
                            }

                            val channelUsername = broadcastData.username
                            val streamCategory = broadcastData.gameName

                            val titleHtml = """<b><a href="https://www.twitch.tv/$channelLogin">$channelUsername</a></b> - ${broadcastData.title}\n\n"""
                            val categoryHtml = if (streamCategory.isNotEmpty()) ", #${streamCategory.toHashTag()}" else ""
                            val startTimeHtml = localizedString(Strings.STREAM_START_TIME).format(broadcastData.uptime) + "\n\n"
                            val viewersHtml = localizedString(Strings.STREAM_VIEWERS).format(broadcastData.viewerCount) + "\n"

                            replyToMessageWithVideo(clipFilename, "$titleHtml$viewersHtml$startTimeHtml#${channelUsername}$categoryHtml")

                            File(clipFilename).delete() // todo: File(clipFilename).delete()
                        } catch (e: Exception) {
                            log.error("error processing clip for $channelLogin: ${e.message}")
                        }
                    }
                }.joinAll()
            } finally {
                deleteMessage(tempMessageId)
            }
        }
    }
}
