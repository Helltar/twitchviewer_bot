package com.helltar.twitchviewerbot.commands.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand
import com.helltar.twitchviewerbot.twitch.Twitch
import com.helltar.twitchviewerbot.utils.Utils.escapeHtml
import com.helltar.twitchviewerbot.utils.Utils.getTimeZoneOffset

class LiveCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    private val thumbnailsUrls = hashMapOf<String, String>()

    override fun run() {
        if (args.isEmpty())
            if (isUserListNotEmpty())
                sendOnlineList(getUserChannelsList())
            else
                replyToMessage(localizedString(Strings.live_command_info))
        else {
            val channel = args.first()

            if (isChannelNameValid(channel))
                sendOnlineList(channel)
        }
    }

    fun sendOnlineList(channels: List<String>) {
        val isNotOneChannel = channels.size > 1

        val waitText =
            if (isNotOneChannel)
                localizedString(Strings.wait_check_online)
            else
                String.format(localizedString(Strings.wait_check_user_online), channels.first())

        val waitMessageId = replyToMessage(waitText)
        var liveList = getOnlineList(channels)
        deleteMessage(waitMessageId)

        var isStreamsAvailable = true

        if (liveList.isEmpty()) {
            liveList =
                if (isNotOneChannel)
                    localizedString(Strings.empty_online_list)
                else
                    localizedString(Strings.stream_offline)

            isStreamsAvailable = false
        }

        val liveListMessageId = replyToMessage(liveList)

        if (isStreamsAvailable) {
            thumbnailsUrls.forEach { replyToMessageWithPhoto(it.value + "?t=${System.currentTimeMillis()}", it.key, liveListMessageId) }
            thumbnailsUrls.clear()
        }
    }

    private fun sendOnlineList(channel: String) =
        sendOnlineList(listOf(channel))

    private fun getOnlineList(userLogins: List<String>): String {
        val list =
            Twitch().getOnlineList(userLogins)
                ?: return localizedString(Strings.twitch_exception)

        val result =
            list.joinToString("\n\n") { streamData ->
                streamData.run {
                    val htmlTitle = "<b><a href=\"https://www.twitch.tv/$login\">$username</a></b> - $title\n\n"
                    val viewers = "\uD83D\uDC40 <b>$viewerCount</b>\n" // 👀
                    val game = if (gameName.isNotEmpty()) "\uD83C\uDFB2 <b>${gameName.escapeHtml()}</b>\n" else "" // 🎲
                    val time = String.format(localizedString(Strings.stream_start_time), startedAt, uptime, getTimeZoneOffset())

                    thumbnailsUrls["#$username - $title"] = thumbnailUrl

                    htmlTitle + viewers + game + time
                }
            }

        return result
    }
}
