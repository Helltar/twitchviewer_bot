package com.helltar.twitchviewerbot.commands.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand
import com.helltar.twitchviewerbot.twitch.Twitch
import com.helltar.twitchviewerbot.twitch.TwitchUtils
import com.helltar.twitchviewerbot.utils.Utils.getTimeZoneOffset
import com.helltar.twitchviewerbot.utils.Utils.replaceTitleTag
import kotlinx.coroutines.*
import java.io.File

class ClipCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    private companion object {
        val requestsList = hashMapOf<String, Job>()
    }

    override fun run() {
        if (args.isEmpty()) {
            if (isUserListNotEmpty())
                getClipsFromAll(getUserChannelsList())
            else
                replyToMessage(localizedString(Strings.clip_command_info))
        } else {
            val channel = args.first()

            if (isChannelNameValid(channel))
                getClip(channel)
        }
    }

    fun getClipsFromAll(userLogins: List<String>) {
        Twitch().getOnlineList(userLogins)?.let {
            if (it.isNotEmpty())
                sendClips(it)
            else {
                val text = if (userLogins.size > 1)
                    localizedString(Strings.empty_online_list)
                else
                    String.format(localizedString(Strings.stream_offline), userLogins.first())

                replyToMessage(text)
            }
        }
            ?: replyToMessage(localizedString(Strings.twitch_exception))
    }

    private fun getClip(channel: String) =
        getClipsFromAll(listOf(channel))

    private fun sendClips(twitchBroadcastData: List<Twitch.BroadcastData>) {
        twitchBroadcastData.forEach { broadcastData ->
            val channelLogin = broadcastData.login
            val channelUsername = broadcastData.username
            val channelLink = "<a href=\"https://www.twitch.tv/$channelLogin\">$channelUsername</a>"

            val requestKey = "$userId@$channelLogin"
            val tempMessage = localizedString(Strings.start_get_clip).format(channelLink)

            addRequest(requestKey) {
                val tempMessageId = replyToMessage(tempMessage)

                try {
                    val clipFilename = TwitchUtils.getShortClip(channelLogin)

                    if (!File(clipFilename).exists()) {
                        replyToMessage(localizedString(Strings.get_clip_fail))
                        return@addRequest
                    }

                    val streamTitle = broadcastData.title
                    val streamCategory = broadcastData.gameName
                    val viewerCount = broadcastData.viewerCount
                    val startedAt = broadcastData.startedAt
                    val streamUptime = broadcastData.uptime

                    val viewersHtml = "\uD83D\uDC40 <b>$viewerCount</b>\n" // 👀
                    val startTimeHtml = localizedString(Strings.stream_start_time).format(startedAt, streamUptime, getTimeZoneOffset()) + "\n\n"
                    val categoryHtml = if (streamCategory.isNotEmpty()) ", #${streamCategory.replaceTitleTag()}" else ""
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
                replyToMessage(localizedString(Strings.many_request))
                return
            }
        }

        requestsList[requestKey] = CoroutineScope(Dispatchers.IO).launch(CoroutineName(requestKey)) { block() }
    }
}