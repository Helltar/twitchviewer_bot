package com.helltar.twitchviewerbot.commands.twitch

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand
import com.helltar.twitchviewerbot.twitch.Twitch
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import java.net.URI

class LiveCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    override suspend fun run() {
        if (arguments.isEmpty()) {
            if (isUserListNotEmpty())
                fetchAndSendLiveList(getUserChannelsList())
            else
                replyToMessage(localizedString(Strings.LIVE_COMMAND_INFO))
        } else {
            val channel = arguments.first()

            if (checkChannelNameAndReplyIfInvalid(channel))
                fetchAndSendLiveList(channel)
        }
    }

    fun fetchAndSendLiveList(channels: List<String>) {
        val tempMessageId = replyToMessage(localizedString(Strings.WAIT_CHECK_ONLINE))

        try {
            val liveList = twitch.getOnlineList(channels)

            if (liveList != null) {
                if (liveList.isNotEmpty()) {
                    val chunks = liveList.chunked(10)

                    chunks.forEach { chunk ->
                        if (chunk.size > 1)
                            replyToMessageWithMediaGroup(chunk.map { buildMediaPhoto(it) })
                        else {
                            val broadcastData = chunk.first()
                            replyToMessageWithPhoto(broadcastData.thumbnailUrl, createHtmlCaption(broadcastData))
                        }
                    }
                } else
                    replyToMessage(localizedString(Strings.EMPTY_ONLINE_LIST))
            } else
                replyToMessage(localizedString(Strings.TWITCH_EXCEPTION))
        } finally {
            deleteMessage(tempMessageId)
        }
    }

    private fun fetchAndSendLiveList(channel: String) =
        fetchAndSendLiveList(listOf(channel))

    private fun buildMediaPhoto(broadcastData: Twitch.BroadcastData): InputMediaPhoto {
        val caption = createHtmlCaption(broadcastData)
        val inputStream = URI.create(broadcastData.thumbnailUrl).toURL().openStream()

        return InputMediaPhoto.builder()
            .media(inputStream, broadcastData.thumbnailUrl)
            .caption(caption)
            .parseMode(ParseMode.HTML)
            .build()
    }
}
