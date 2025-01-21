package com.helltar.twitchviewerbot.db.dao

import com.helltar.twitchviewerbot.db.DatabaseFactory.dbQuery
import com.helltar.twitchviewerbot.db.tables.UserChannelsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Clock
import java.time.Instant

class UserChannelsDao {

    suspend fun add(userId: Long, channel: String) = dbQuery {
        UserChannelsTable.insertIgnore {
            it[this.userId] = userId
            it[this.channel] = channel
            it[added] = Instant.now(Clock.systemUTC())
        }.insertedCount > 0
    }

    suspend fun delete(userId: Long, channel: String) = dbQuery {
        UserChannelsTable
            .deleteWhere { UserChannelsTable.userId eq userId and (UserChannelsTable.channel eq channel) } > 0
    }

    suspend fun getChannels(userId: Long) = dbQuery {
        UserChannelsTable
            .select(UserChannelsTable.channel)
            .where { UserChannelsTable.userId eq userId }
            .map { it[UserChannelsTable.channel] }
    }

    suspend fun isChannelsListNotEmpty(userId: Long) = dbQuery {
        UserChannelsTable
            .select(UserChannelsTable.userId)
            .where { UserChannelsTable.userId eq userId }
            .count() > 0
    }

    suspend fun isChannelsListEmpty(userId: Long) =
        !isChannelsListNotEmpty(userId)
}

val userChannelsDao = UserChannelsDao()
