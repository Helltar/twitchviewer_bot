package com.helltar.twitchviewer_bot

import com.helltar.twitchviewer_bot.utils.Utils.getLineFromFile
import java.io.File

object BotConfig {

    private const val DIR_CONFIG = "config/"
    private const val DIR_DATABASE = "db/"

    const val EXT_XML = ".xml"
    const val DIR_TEMP = "temp/"
    const val DIR_LOCALE = "locale/"
    const val DIR_DB_USER_LIST = DIR_DATABASE + "user_list/"
    const val DIR_DB_USER = DIR_DATABASE + "user/"

    val IFDEF_DEBUG = File(DIR_CONFIG + "debug").exists()
    val BOT_TOKEN = getLineFromFile(DIR_CONFIG + "bot_token.txt")
    val TWITCH_TOKEN = getLineFromFile(DIR_CONFIG + "twitch_token.txt")
}
