package com.helltar.twitchviewer_bot

import com.helltar.twitchviewer_bot.utils.Utils.getLineFromFile

object BotConfig {

    const val EXT_XML = ".xml"
    const val DIR_TEMP = "temp/"
    const val DIR_LOCALE = "locale/"
    const val DIR_DB = "db/"

    const val FILE_DB_USERS = DIR_DB + "users.db"

    val BOT_TOKEN = getLineFromFile("config/bot_token.txt")
    val TWITCH_TOKEN = getLineFromFile("config/twitch_token.txt")
}
