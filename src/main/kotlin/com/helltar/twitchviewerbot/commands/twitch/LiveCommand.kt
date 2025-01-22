package com.helltar.twitchviewerbot.commands.twitch

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand
import com.helltar.twitchviewerbot.twitch.Utils.createTwitchHtmlLink
import com.helltar.twitchviewerbot.twitch.Utils.escapeHtml

class LiveCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    private val thumbnailUrls = hashMapOf<String, String>()

    override suspend fun run() {
        if (arguments.isEmpty()) {
            if (isUserListNotEmpty())
                sendOnlineList(getUserChannelsList())
            else
                replyToMessage(localizedString(Strings.LIVE_COMMAND_INFO))
        } else {
            val channel = arguments.first()

            if (checkChannelNameAndReplyIfInvalid(channel))
                sendOnlineList(channel)
        }
    }

    fun sendOnlineList(channels: List<String>) {
        val isNotOneChannel = channels.size > 1

        val tempMessageText =
            if (isNotOneChannel)
                localizedString(Strings.WAIT_CHECK_ONLINE)
            else
                localizedString(Strings.WAIT_CHECK_USER_ONLINE).format(channels.first())

        val tempMessageId = replyToMessage(tempMessageText)

        try {
            var listHtml = getOnlineListHtml(channels)
            var isStreamsAvailable = true

            if (listHtml.isEmpty()) {
                listHtml =
                    if (isNotOneChannel)
                        localizedString(Strings.EMPTY_ONLINE_LIST)
                    else
                        localizedString(Strings.STREAM_OFFLINE)

                isStreamsAvailable = false
            }

            val messageId = replyToMessage(listHtml)

            if (isStreamsAvailable) {
                thumbnailUrls.forEach { replyToMessageWithPhoto(it.value + "?t=${System.currentTimeMillis()}", it.key, messageId) }
                thumbnailUrls.clear()
            }
        } finally {
            deleteMessage(tempMessageId)
        }
    }

    private fun sendOnlineList(channel: String) =
        sendOnlineList(listOf(channel))

    private fun getOnlineListHtml(userLogins: List<String>): String {
        val list =
            twitch.getOnlineList(userLogins)
                ?: return localizedString(Strings.TWITCH_EXCEPTION)

        val result =
            list.joinToString("\n\n") { streamData ->
                streamData.run {
                    val htmlTitle = "${createTwitchHtmlLink(login, username)} - $title\n\n"
                    val viewers = "\uD83D\uDC40 <b>$viewerCount</b>\n" // 👀
                    val game = if (gameName.isNotEmpty()) "\uD83C\uDFB2 <b>${gameName.escapeHtml()}</b>\n" else "" // 🎲
                    val time = localizedString(Strings.STREAM_START_TIME).format(uptime)

                    thumbnailUrls["#$username - $title"] = thumbnailUrl

                    htmlTitle + viewers + game + time
                }
            }

        return result
    }
}
