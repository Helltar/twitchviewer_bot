package com.helltar.twitchviewerbot

import io.github.cdimascio.dotenv.dotenv

object EnvConfig {

    val creatorId = readEnv("CREATOR_ID").toLongOrNull() ?: throw RuntimeException("bad CREATOR_ID env")
    val botToken = readEnv("BOT_TOKEN")
    val botUsername = readEnv("BOT_USERNAME")
    val twitchToken = readEnv("TWITCH_TOKEN")

    private fun readEnv(env: String) =
        dotenv { ignoreIfMissing = true }[env] ?: throw RuntimeException("error when read $env env")
}