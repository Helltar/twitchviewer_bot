package com.helltar.twitchviewer_bot

import com.helltar.twitchviewer_bot.utils.Utils.getLineFromFile
import java.lang.RuntimeException

object BotConfig {

    const val EXT_XML = ".xml"
    const val DIR_TEMP = "temp/"
    const val DIR_LOCALE = "locale/"
    const val DIR_DB = "db/"
    const val FILE_DB_USERS = DIR_DB + "users.db"

    private const val DIR_CONFIG = "config/"
    private const val FILE_CREATOR_ID = DIR_CONFIG + "creator_id.txt"
    private const val FILE_BOT_TOKEN = DIR_CONFIG + "bot_token.txt"
    private const val FILE_TWITCH_TOKEN = DIR_CONFIG + "twitch_token.txt"
    private const val FILE_BOT_USERNAME = DIR_CONFIG + "bot_username.txt"

    val CREATOR_ID =
        getLineFromFile(FILE_CREATOR_ID).toLongOrNull() ?: throw RuntimeException("Bad $FILE_CREATOR_ID")

    val BOT_TOKEN =
        getLineFromFile(FILE_BOT_TOKEN).ifEmpty { throw RuntimeException("$FILE_BOT_TOKEN is empty") }

    val BOT_USERNAME =
        getLineFromFile(FILE_BOT_USERNAME).ifEmpty { throw RuntimeException("$FILE_BOT_USERNAME is empty") }

    val TWITCH_TOKEN =
        getLineFromFile(FILE_TWITCH_TOKEN).ifEmpty { throw RuntimeException("$FILE_TWITCH_TOKEN is empty") }
}
