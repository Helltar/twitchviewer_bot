package com.helltar.twitchviewerbot.commands.twitch.keyboard

object ButtonCallbacks {

    const val BUTTON_BACK = "1"
    const val BUTTON_CHANNEL = "2"
    const val BUTTON_CLIP = "3"
    const val BUTTON_CLIPS = "4"
    const val BUTTON_CLOSE_LIST = "5"
    const val BUTTON_DELETE_CHANNEL = "6"
    const val BUTTON_SCREEN = "7"
    const val BUTTON_UPDATE = "9"
    const val BUTTON_NEXT_PAGE = "10"
    const val BUTTON_PREV_PAGE = "11"

    data class CallbackData(
        val buttonId: String = BUTTON_CLIP,
        val ownerId: Long = 123,
        val channelName: String = "null",
        val isStreamLive: Boolean = false,
        val navigationPage: Int = 0
    )

    fun CallbackData.string() =
        this.run { "$buttonId $ownerId $channelName ${if (isStreamLive) "1" else "0"} $navigationPage" }

    fun parseOwnerId(callbackData: String) =
        splitData(callbackData)[1].toLongOrNull()
            ?: throw IllegalArgumentException("bad owner id in callback data: $callbackData")

    fun parseChannelName(callbackData: String) =
        splitData(callbackData)[2].ifBlank {
            throw IllegalArgumentException("bad channel name in callback data: $callbackData")
        }

    fun parseStreamLiveState(callbackData: String) =
        (splitData(callbackData)[3].toIntOrNull() ?: 0) != 0

    fun parseNavigationPage(callbackData: String) =
        splitData(callbackData)[4].toIntOrNull() ?: 0

    private fun splitData(data: String) =
        data.split("\\s+".toRegex())
}
