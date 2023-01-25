package com.helltar.twitchviewer_bot.commands.commands

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.commands.TwitchCommand
import com.helltar.twitchviewer_bot.utils.Utils

class LiveCommand(bot: Bot, message: Message, args: List<String> = listOf()) : TwitchCommand(bot, message, args) {

    private val thumbnailUrls = hashMapOf<String, String>()

    override fun run() {
        if (args.isEmpty())
            if (isUserListNotEmpty())
                sendOnlineList(getUserChannelsList())
            else
                sendMessage(localizedString(Strings.live_command_info))
        else
            sendOnlineList(if (checkIsChannelNameValid()) listOf(args[0]) else return)
    }

    fun sendOnlineList(userLogins: List<String>) {
        val isNotOneChannel = userLogins.size > 1

        val waitText = if (isNotOneChannel)
            localizedString(Strings.wait_check_online)
        else
            String.format(localizedString(Strings.wait_check_user_online), userLogins[0])

        val waitMessageId = sendMessage(waitText)
        var liveList = getOnlineList(userLogins)
        deleteMessage(waitMessageId)

        var isStreamsAvailable = true

        if (liveList.isEmpty()) {
            liveList = if (isNotOneChannel)
                localizedString(Strings.empty_online_list)
            else
                localizedString(Strings.stream_offline)

            isStreamsAvailable = false
        }

        val liveListMessageId = sendMessage(liveList)

        if (isStreamsAvailable) {
            thumbnailUrls.forEach {
                sendPhoto(it.value + "?t=${System.currentTimeMillis()}", it.key, liveListMessageId)
            }

            thumbnailUrls.clear()
        }
    }

    private fun getOnlineList(userLogins: List<String>): String {
        var result = ""

        val list = twitch.getOnlineList(userLogins) ?: return localizedString(Strings.twitch_exception)

        list.forEach {
            val username = Utils.escapeHtml(it.username)
            val title = Utils.escapeHtml(it.title)
            val htmlTitle = "<b><a href=\"https://www.twitch.tv/${it.login}\">$username</a></b> - $title\n\n"
            val viewerCount = "\uD83D\uDC64 <b>${it.viewerCount}</b>\n" // 👤
            val gameName = if (it.gameName.isNotEmpty()) "\uD83C\uDFB2 <b>${Utils.escapeHtml(it.gameName)}</b>\n" else "" // 🎲
            val time = String.format(localizedString(Strings.stream_start_time), it.startedAt, it.uptime) + "\n\n"

            thumbnailUrls["#$username - $title"] = it.thumbnailUrl

            result += htmlTitle + viewerCount + gameName + time
        }

        return result
    }
}
