package com.helltar.twitchviewer_bot.keyboard

object BtnCallbacks {

    const val BUTTON_BACK = "1"
    const val BUTTON_CHANNEL = "2"
    const val BUTTON_CLIP = "5"
    const val BUTTON_CLIPS = "6"
    const val BUTTON_CLOSE_LIST = "3"
    const val BUTTON_DELETE_CHANNEL = "4"
    const val BUTTON_LIVE = "8"
    const val BUTTON_SCREENSHOT = "9"
    const val BUTTON_SHOW = "7"
    const val BUTTON_UPDATE = "10"

    data class CallbackData(
        val btnActName: String,
        val ownerId: Long?,
        val channelName: String = "-",
        val isChannelLive: Int = 0
    )

    fun getButtonNameFromCbData(data: String) = parseCbData(data)[0]
    fun getOwnerIdFromCbData(data: String) = parseCbData(data)[1].toLongOrNull()
    fun getChannelNameFromCbData(data: String) = parseCbData(data)[2]
    fun getChannelStatusFromCbData(data: String) = (parseCbData(data)[3].toIntOrNull() ?: 0) != 0

    private fun parseCbData(data: String) = data.split("\\s+".toRegex()).toList()
}