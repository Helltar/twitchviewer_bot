package com.helltar.twitchviewerbot.dao.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object UsersTable : Table() {

    private val id = integer("id").autoIncrement()

    val userId = long("userId").uniqueIndex()
    val firstName = varchar("firstName", 64)
    val username = varchar("username", 32).nullable()
    val languageCode = varchar("languageCode", 16).nullable()
    val joinedDate = datetime("joinedDate")
    val lastUsageDate = datetime("lastUsageDate")

    override val primaryKey = PrimaryKey(id)
}