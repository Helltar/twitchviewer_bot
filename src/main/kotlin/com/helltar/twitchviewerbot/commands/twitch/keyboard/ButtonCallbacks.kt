package com.helltar.twitchviewerbot.commands.twitch.keyboard

object ButtonCallbacks {

    const val BUTTON_BACK = "1"
    const val BUTTON_CHANNEL = "2"
    const val BUTTON_CLIP = "3"
    const val BUTTON_CLIPS = "4"
    const val BUTTON_CLOSE_LIST = "5"
    const val BUTTON_DELETE_CHANNEL = "6"
    const val BUTTON_LIVE = "7"
    const val BUTTON_SCREENSHOT = "8"
    const val BUTTON_UPDATE = "9"

    data class CallbackData(
        val command: String = BUTTON_CLIP,
        val ownerId: Long = 123,
        val channel: String = "null",
        val isStreamLive: Boolean = false
    )

    fun CallbackData.string() =
        this.run { "$command $ownerId $channel ${if (isStreamLive) "1" else "0"}" }

    fun getOwnerId(data: String) =
        splitData(data)[1].toLongOrNull()
            ?: throw IllegalArgumentException("bad owner id in callback data: $data")

    fun getChannelName(data: String) =
        splitData(data)[2].ifBlank {
            throw IllegalArgumentException("bad channel name in callback data: $data")
        }

    fun isStreamLive(data: String) =
        (splitData(data)[3].toIntOrNull() ?: 0) != 0

    private fun splitData(data: String) =
        data.split("\\s+".toRegex())
}