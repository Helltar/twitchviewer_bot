package com.helltar.twitchviewerbot.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object UserChannelsTable : Table() {

    private val id = integer("id").autoIncrement()

    val userId = long("user_id").references(UsersTable.userId, onDelete = ReferenceOption.CASCADE).index()
    val channel = varchar("channel", 32)
    val added = timestamp("added")

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(userId, channel)
    }
}
