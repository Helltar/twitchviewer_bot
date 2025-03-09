package com.helltar.twitchviewerbot.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : IntIdTable() {

    val userId = long("user_id").uniqueIndex()

    val firstName = varchar("first_name", 64)
    val username = varchar("username", 32).nullable()
    val languageCode = varchar("language_code", 20).nullable()

    val updatedAt = timestamp("updated_at").nullable()
    val createdAt = timestamp("created_at")
}
