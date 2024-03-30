package com.helltar.twitchviewerbot.command.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Extensions.toHashTag
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.command.TwitchCommand
import com.helltar.twitchviewerbot.twitch.Twitch
import com.helltar.twitchviewerbot.twitch.TwitchUtils
import kotlinx.coroutines.*
import java.awt.SystemColor.text
import java.io.File

class ClipCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    private companion object {
        val requestsList = hashMapOf<String, Job>()
    }

    override fun run() {
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

    fun getClipsFromAll(userLogins: List<String>) {
        twitch.getOnlineList(userLogins)?.let {
            if (it.isNotEmpty())
                sendClips(it)
            else {
                val text = if (userLogins.size > 1)
                    localizedString(Strings.EMPTY_ONLINE_LIST)
                else
                    localizedString(Strings.STREAM_OFFLINE.format(userLogins.first()))

                replyToMessage(text)
            }
        }
            ?: replyToMessage(localizedString(Strings.TWITCH_EXCEPTION))
    }

    private fun getClip(channel: String) =
        getClipsFromAll(listOf(channel))

    private fun sendClips(twitchBroadcastData: List<Twitch.BroadcastData>) {
        twitchBroadcastData.forEach { broadcastData ->
            val channelLogin = broadcastData.login
            val channelUsername = broadcastData.username
            val channelLink = "<a href=\"https://www.twitch.tv/$channelLogin\">$channelUsername</a>"

            val requestKey = "$userId@$channelLogin"
            val tempMessage = localizedString(Strings.START_GET_CLIP).format(channelLink)

            addRequest(requestKey) {
                val tempMessageId = replyToMessage(tempMessage)

                try {
                    val clipFilename = TwitchUtils.getShortClip(channelLogin)

                    if (!File(clipFilename).exists()) {
                        replyToMessage(localizedString(Strings.GET_CLIP_FAIL))
                        return@addRequest
                    }

                    val streamTitle = broadcastData.title
                    val streamCategory = broadcastData.gameName
                    val viewerCount = broadcastData.viewerCount
                    val startedAt = broadcastData.startedAt
                    val streamUptime = broadcastData.uptime

                    val viewersHtml = "\uD83D\uDC40 <b>$viewerCount</b>\n" // 👀
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

    private fun addRequest(requestKey: String, block: () -> Unit) {
        if (requestsList.containsKey(requestKey)) {
            if (requestsList[requestKey]?.isCompleted == false) {
                replyToMessage(localizedString(Strings.MANY_REQUEST))
                return
            }
        }

        requestsList[requestKey] = CoroutineScope(Dispatchers.IO).launch(CoroutineName(requestKey)) { block() }
    }
}