package com.helltar.twitchviewerbot.dao.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : Table() {

    val userId = long("user_id")
    val firstName = varchar("first_name", 64)
    val username = varchar("username", 32).nullable()
    val languageCode = varchar("language_code", 20).nullable()
    val joined = timestamp("joined")
    val lastUsage = timestamp("last_usage")

    override val primaryKey = PrimaryKey(userId)
}