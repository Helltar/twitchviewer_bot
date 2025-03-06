package com.helltar.twitchviewerbot.database.dao

import com.helltar.twitchviewerbot.database.Database.dbTransaction
import com.helltar.twitchviewerbot.database.Database.utcNow
import com.helltar.twitchviewerbot.database.tables.UserChannelsTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore

class UserChannelsDao {

    suspend fun add(userId: Long, channelName: String): Boolean = dbTransaction {
        UserChannelsTable
            .insertIgnore {
                it[this.userId] = userId
                it[this.channelName] = channelName
                it[this.createdAt] = utcNow()
            }
            .insertedCount > 0
    }

    suspend fun delete(userId: Long, channelName: String): Boolean = dbTransaction {
        UserChannelsTable
            .deleteWhere {
                UserChannelsTable.userId eq userId and
                        (UserChannelsTable.channelName eq channelName)
            } > 0
    }

    suspend fun channels(userId: Long): List<String> = dbTransaction {
        UserChannelsTable
            .select(UserChannelsTable.channelName)
            .where { UserChannelsTable.userId eq userId }
            .map { it[UserChannelsTable.channelName] }
    }

    suspend fun userHasChannels(userId: Long): Boolean = dbTransaction {
        UserChannelsTable
            .select(UserChannelsTable.userId)
            .where { UserChannelsTable.userId eq userId }
            .count() > 0
    }

    suspend fun doesUserHaveNoChannels(userId: Long): Boolean =
        !userHasChannels(userId)
}

val userChannelsDao = UserChannelsDao()
