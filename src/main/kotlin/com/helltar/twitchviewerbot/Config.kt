package com.helltar.twitchviewerbot

import io.github.cdimascio.dotenv.dotenv

object Config {

    const val DIR_TEMP = "temp"
    const val DIR_LOCALE = "locale"
    const val DIR_DB = "database"
    const val DATABASE_FILE = "./$DIR_DB/database"

    val creatorId = readEnv("CREATOR_ID").toLongOrNull() ?: throw RuntimeException("bad creator id")
    val botToken = readEnv("BOT_TOKEN")
    val botUsername = readEnv("BOT_USERNAME")
    val twitchToken = readEnv("TWITCH_TOKEN")

    private fun readEnv(env: String) =
        dotenv()[env] ?: throw RuntimeException("error when read $env env")
}