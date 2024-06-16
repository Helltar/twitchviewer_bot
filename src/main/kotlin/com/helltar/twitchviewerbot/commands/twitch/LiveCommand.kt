package com.helltar.twitchviewerbot.commands.twitch

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Extensions.escapeHtml
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand

class LiveCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    private val thumbnailsUrls = hashMapOf<String, String>()

    override suspend fun run() {
        if (arguments.isEmpty()) {
            if (isUserListNotEmpty())
                sendOnlineList(getUserChannelsList())
            else
                replyToMessage(localizedString(Strings.LIVE_COMMAND_INFO))
        } else {
            val channel = arguments.first()

            if (isChannelNameValid(channel))
                sendOnlineList(channel)
        }
    }

    fun sendOnlineList(channels: List<String>) {
        val isNotOneChannel = channels.size > 1

        val waitText =
            if (isNotOneChannel)
                localizedString(Strings.WAIT_CHECK_ONLINE)
            else
                localizedString(Strings.WAIT_CHECK_USER_ONLINE).format(channels.first())

        val waitMessageId = replyToMessage(waitText)
        var liveList = getOnlineList(channels)
        deleteMessage(waitMessageId)

        var isStreamsAvailable = true

        if (liveList.isEmpty()) {
            liveList =
                if (isNotOneChannel)
                    localizedString(Strings.EMPTY_ONLINE_LIST)
                else
                    localizedString(Strings.STREAM_OFFLINE)

            isStreamsAvailable = false
        }

        val liveListMessageId = replyToMessage(liveList)

        if (isStreamsAvailable) {
            thumbnailsUrls.forEach {
                replyToMessageWithPhoto(it.value + "?t=${System.currentTimeMillis()}", it.key, liveListMessageId)
            }

            thumbnailsUrls.clear()
        }
    }

    private fun sendOnlineList(channel: String) =
        sendOnlineList(listOf(channel))

    private fun getOnlineList(userLogins: List<String>): String {
        val list =
            twitch.getOnlineList(userLogins)
                ?: return localizedString(Strings.TWITCH_EXCEPTION)

        val result =
            list.joinToString("\n\n") { streamData ->
                streamData.run {
                    val htmlTitle = "<b><a href=\"https://www.twitch.tv/$login\">$username</a></b> - $title\n\n"
                    val viewers = "\uD83D\uDC40 <b>$viewerCount</b>\n" // 👀
                    val game = if (gameName.isNotEmpty()) "\uD83C\uDFB2 <b>${gameName.escapeHtml()}</b>\n" else "" // 🎲
                    val time = localizedString(Strings.STREAM_START_TIME).format(startedAt, uptime, getTimeZoneOffset())

                    thumbnailsUrls["#$username - $title"] = thumbnailUrl

                    htmlTitle + viewers + game + time
                }
            }

        return result
    }
}