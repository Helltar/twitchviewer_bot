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
            val requestKey = "$userId@${broadcastData.login}"

            addRequest(requestKey) {
                val tempMessage = String.format(localizedString(Strings.start_get_clip), broadcastData.username)
                val tempMessageId = replyToMessage(tempMessage)
                val clipFilename = TwitchUtils.getShortClip(broadcastData.login)

                if (!File(clipFilename).exists()) {
                    replyToMessage(localizedString(Strings.get_clip_fail))
                    deleteMessage(tempMessageId)
                    return@addRequest
                }

                broadcastData.run {
                    val htmlTitle = "<b><a href=\"https://www.twitch.tv/$login\">$username</a></b> - $title\n\n"
                    val viewers = "\uD83D\uDC40 <b>$viewerCount</b>\n" // 👀
                    val time = String.format(localizedString(Strings.stream_start_time), startedAt, getTimeZoneOffset(), uptime) + "\n\n"
                    val gameName = if (gameName.isNotEmpty()) ", #${gameName.replaceTitleTag()}" else ""

                    replyToMessageWithVideo(clipFilename, "$htmlTitle$viewers$time#${username}$gameName")
                }

                File(clipFilename).delete()

                deleteMessage(tempMessageId)
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
