package com.helltar.twitchviewerbot.commands.twitch

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Extensions.toHashTag
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand
import com.helltar.twitchviewerbot.twitch.Twitch
import com.helltar.twitchviewerbot.twitch.Utils
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

class ClipCommand(ctx: MessageContext) : TwitchCommand(ctx) {

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
        twitchBroadcastData.forEach { broadcastData ->
            val channelLogin = broadcastData.login
            val channelUsername = broadcastData.username
            val channelLink = """<a href="https://www.twitch.tv/$channelLogin">$channelUsername</a>"""

            val tempMessage = localizedString(Strings.START_GET_CLIP).format(channelLink)

            launch {
                val tempMessageId = replyToMessage(tempMessage)

                try {
                    val clipFilename = Utils.getShortClip(channelLogin)

                    if (!File(clipFilename).exists()) {
                        replyToMessage(localizedString(Strings.GET_CLIP_FAIL))
                        return@launch
                    }

                    val streamTitle = broadcastData.title
                    val streamCategory = broadcastData.gameName
                    val viewerCount = broadcastData.viewerCount
                    val startedAt = broadcastData.startedAt
                    val streamUptime = broadcastData.uptime

                    val viewersHtml = localizedString(Strings.STREAM_VIEWERS).format(viewerCount) + "\n"
                    val startTimeHtml = localizedString(Strings.STREAM_START_TIME).format(startedAt, streamUptime, getTimeZoneOffset()) + "\n\n"
                    val categoryHtml = if (streamCategory.isNotEmpty()) ", #${streamCategory.toHashTag()}" else ""
                    val titleHtml = "<b>$channelLink</b> - $streamTitle\n\n"

                    replyToMessageWithVideo(clipFilename, "$titleHtml$viewersHtml$startTimeHtml#${channelUsername}$categoryHtml")

                    File(clipFilename).delete()
                } finally {
                    deleteMessage(tempMessageId)
                }
            }
        }
    }
}