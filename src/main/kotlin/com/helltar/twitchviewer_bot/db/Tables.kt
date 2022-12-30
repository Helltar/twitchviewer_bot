package com.helltar.twitchviewer_bot.db

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val userId = long("id")
    val firstName = varchar("firstName", 64)
    val username = varchar("username", 32)
    val languageCode = varchar("languageCode", 16)
    override val primaryKey = PrimaryKey(userId)
}

object UserChannels : Table() {
    val userId = long("userId")
    val channels = text("channels")
    override val primaryKey = PrimaryKey(userId)
}
