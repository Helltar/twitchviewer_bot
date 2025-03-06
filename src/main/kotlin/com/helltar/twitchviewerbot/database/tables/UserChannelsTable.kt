package com.helltar.twitchviewerbot.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp

object UserChannelsTable : IntIdTable() {

    val userId = reference("user_id", UsersTable.userId, onDelete = ReferenceOption.CASCADE)
    val channelName = varchar("channel_name", 50) // todo: ChannelsTable
    val createdAt = timestamp("created_at")

    init {
        uniqueIndex(userId, channelName)
    }
}
