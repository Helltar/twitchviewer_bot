package com.helltar.twitchviewerbot

import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.FileReader
import java.lang.RuntimeException

object BotConfig {

    private val log = LoggerFactory.getLogger(javaClass)

    private const val DIR_CONFIG = "config"

    const val DIR_TEMP = "temp"
    const val DIR_LOCALE = "locale"
    const val DIR_DB = "database"

    const val EXT_XML = ".xml"
    const val DATABASE_FILE = "./$DIR_DB/database"

    private const val FILE_CREATOR_ID = "$DIR_CONFIG/creator_id.txt"
    private const val FILE_BOT_TOKEN = "$DIR_CONFIG/bot_token.txt"
    private const val FILE_TWITCH_TOKEN = "$DIR_CONFIG/twitch_token.txt"
    private const val FILE_BOT_USERNAME = "$DIR_CONFIG/bot_username.txt"

    val creatorId = getLineFromFile(FILE_CREATOR_ID).toLongOrNull() ?: throw RuntimeException("bad $FILE_CREATOR_ID")
    val botToken = getLineFromFile(FILE_BOT_TOKEN)
    val botUsername = getLineFromFile(FILE_BOT_USERNAME)
    val twitchToken = getLineFromFile(FILE_TWITCH_TOKEN)

    private fun getLineFromFile(filename: String): String =
        try {
            BufferedReader(FileReader(filename)).readLine()
        } catch (e: Exception) {
            log.error(e.message)
            throw RuntimeException("$filename is empty")
        }
}