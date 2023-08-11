package com.helltar.twitchviewer_bot.commands

import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.BUTTON_CLIPS
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.BUTTON_LIVE
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.BUTTON_SCREENSHOT

object Commands {

    const val COMMAND_ADD = "cmdAdd"
    const val COMMAND_LIST = "cmdList"
    const val COMMAND_START = "cmdStart"
    const val COMMAND_UPTIME = "cmdUptime"
    const val COMMAND_ABOUT = "cmdAbout"
    const val COMMAND_CLIP_COMPRESS = "cmdCompress"

    const val COMMAND_CLIP = BUTTON_CLIPS
    const val COMMAND_LIVE = BUTTON_LIVE
    const val COMMAND_SCREENSHOT = BUTTON_SCREENSHOT
}