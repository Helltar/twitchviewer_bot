package com.helltar.twitchviewerbot.dao

import com.helltar.twitchviewerbot.dao.DatabaseFactory.dbQuery
import com.helltar.twitchviewerbot.dao.tables.UsersChannelsTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.LocalDateTime

class UserChannels {

    fun add(userId: Long, channel: String) = dbQuery {
        UsersChannelsTable.insert {
            it[this.userId] = userId
            it[this.channel] = channel
            it[datetime] = LocalDateTime.now()
        }
            .insertedCount > 0
    }

    fun delete(userId: Long, channel: String) = dbQuery {
        UsersChannelsTable.deleteWhere { UsersChannelsTable.userId eq userId and (UsersChannelsTable.channel eq channel) } > 0
    }

    fun isChannelNotExists(userId: Long, channel: String) = dbQuery {
        !isChannelExists(userId, channel)
    }

    fun getUserChannelsList(userId: Long) =
        getAllChannels(userId).map { it[UsersChannelsTable.channel] }

    fun isUserListEmpty(userId: Long) =
        !isUserListNotEmpty(userId)

    fun isUserListNotEmpty(userId: Long) =
        getAllChannels(userId).isNotEmpty()

    private fun getAllChannels(userId: Long) = dbQuery {
        UsersChannelsTable.select { UsersChannelsTable.userId eq userId }.toList()
    }

    private fun isChannelExists(userId: Long, channel: String) = dbQuery {
        UsersChannelsTable.select { UsersChannelsTable.userId eq userId and (UsersChannelsTable.channel eq channel) }.count() > 0
    }
}