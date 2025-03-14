package com.helltar.twitchviewerbot.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : Table() {

    val userId = long("user_id")
    val firstName = varchar("first_name", 64)
    val username = varchar("username", 32).nullable()
    val languageCode = varchar("language_code", 20).nullable()

    val updatedAt = timestamp("updated_at").nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(userId)
}
