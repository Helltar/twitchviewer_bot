package com.helltar.twitchviewer_bot.commands.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.commands.TwitchCommand
import com.helltar.twitchviewer_bot.utils.Utils

class LiveCommand(ctx: MessageContext, args: List<String> = listOf()) : TwitchCommand(ctx, args) {

    private val thumbnailUrls = hashMapOf<String, String>()

    override fun run() {
        if (args.isEmpty())
            if (isUserListNotEmpty())
                sendOnlineList(getUserChannelsList())
            else
                replyToMessage(localizedString(Strings.live_command_info))
        else
            sendOnlineList(if (checkIsChannelNameValid()) listOf(args[0]) else return)
    }

    fun sendOnlineList(userLogins: List<String>) {
        val isNotOneChannel = userLogins.size > 1

        val waitText =
            if (isNotOneChannel)
                localizedString(Strings.wait_check_online)
            else
                String.format(localizedString(Strings.wait_check_user_online), userLogins[0])

        val waitMessageId = replyToMessage(waitText)
        var liveList = getOnlineList(userLogins)
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
            thumbnailUrls.forEach {
                replyToMessageWithPhoto(it.value + "?t=${System.currentTimeMillis()}", it.key, liveListMessageId)
            }

            thumbnailUrls.clear()
        }
    }

    private fun getOnlineList(userLogins: List<String>): String {
        val list =
            twitch.getOnlineList(userLogins)
                ?: return localizedString(Strings.twitch_exception)

        var result = ""

        list.forEach { streamData ->
            streamData.run {
                val htmlTitle = "<b><a href=\"https://www.twitch.tv/$login\">$username</a></b> - $title\n\n"
                val viewers = "\uD83D\uDC40 <b>$viewerCount</b>\n" // 👀
                val game = if (gameName.isNotEmpty()) "\uD83C\uDFB2 <b>${Utils.escapeHtml(gameName)}</b>\n" else "" // 🎲
                val time = String.format(localizedString(Strings.stream_start_time), startedAt, uptime) + "\n\n"

                thumbnailUrls["#$username - $title"] = thumbnailUrl

                result += htmlTitle + viewers + game + time
            }
        }

        return result
    }
}