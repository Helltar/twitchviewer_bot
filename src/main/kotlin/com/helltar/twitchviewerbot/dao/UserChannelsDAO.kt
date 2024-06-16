package com.helltar.twitchviewerbot.dao

import com.helltar.twitchviewerbot.dao.DatabaseFactory.dbQuery
import com.helltar.twitchviewerbot.dao.tables.UserChannelsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Clock
import java.time.Instant

class UserChannelsDAO {

    suspend fun add(userId: Long, channel: String) = dbQuery {
        UserChannelsTable.insertIgnore {
            it[this.userId] = userId
            it[this.channel] = channel
            it[added] = Instant.now(Clock.systemUTC())
        }.insertedCount > 0
    }

    suspend fun delete(userId: Long, channel: String) = dbQuery {
        UserChannelsTable.deleteWhere { UserChannelsTable.userId eq userId and (UserChannelsTable.channel eq channel) } > 0
    }

    suspend fun getChannels(userId: Long) = dbQuery {
        UserChannelsTable.selectAll().where { UserChannelsTable.userId eq userId }.map { it[UserChannelsTable.channel] }
    }

    suspend fun isChannelsListNotEmpty(userId: Long) = dbQuery {
        UserChannelsTable.selectAll().where { UserChannelsTable.userId eq userId }.count() > 0
    }

    suspend fun isChannelsListEmpty(userId: Long) =
        !isChannelsListNotEmpty(userId)
}