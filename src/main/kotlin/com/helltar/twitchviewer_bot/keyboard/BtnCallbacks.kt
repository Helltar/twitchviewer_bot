package com.helltar.twitchviewer_bot.keyboard

object BtnCallbacks {

    const val buttonBack = "1"
    const val buttonChannel = "2"
    const val buttonClip = "5"
    const val buttonClips = "6"
    const val buttonCloseList = "3"
    const val buttonDeleteChannel = "4"
    const val buttonLive = "8"
    const val buttonScreenshot = "9"
    const val buttonShow = "7"
    const val buttonUpdate = "10"

    data class CallbackData(val btnActName: String, val ownerId: Long?, val channelName: String = "-", val isChannelLive: Int = 0)

    fun getButtonNameFromCbData(data: String) = parseCbData(data)[0]
    fun getOwnerIdFromCbData(data: String) = parseCbData(data)[1].toLongOrNull()
    fun getChannelNameFromCbData(data: String) = parseCbData(data)[2]
    fun getChannelStatusFromCbData(data: String) = (parseCbData(data)[3].toIntOrNull() ?: 0) != 0

    private fun parseCbData(data: String) = data.split("\\s+".toRegex()).toList()
}
