package com.helltar.twitchviewerbot

import io.github.cdimascio.dotenv.dotenv

object EnvConfig {

    val creatorId = readEnv("CREATOR_ID").toLongOrNull() ?: throw RuntimeException("bad CREATOR_ID env")
    val botToken = readEnv("BOT_TOKEN")
    val botUsername = readEnv("BOT_USERNAME")
    val twitchToken = readEnv("TWITCH_TOKEN")

    val postgresqlHost = readEnv("POSTGRESQL_HOST")
    val databaseName = readEnv("DATABASE_NAME")
    val databaseUser = readEnv("DATABASE_USER")
    val databasePassword = readEnv("DATABASE_PASSWORD")

    private fun readEnv(env: String) =
        dotenv { ignoreIfMissing = true }[env] ?: throw RuntimeException("error when read $env env")
}