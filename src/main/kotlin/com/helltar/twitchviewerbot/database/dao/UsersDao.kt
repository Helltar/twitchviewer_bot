package com.helltar.twitchviewerbot.database.dao

import com.helltar.twitchviewerbot.database.DatabaseFactory.dbQuery
import com.helltar.twitchviewerbot.database.tables.UsersTable
import com.helltar.twitchviewerbot.database.tables.UsersTable.languageCode
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.update
import org.telegram.telegrambots.meta.api.objects.User
import java.time.Clock
import java.time.Instant

class UsersDao {

    suspend fun add(user: User) = dbQuery {
        UsersTable.insertIgnore {
            it[userId] = user.id
            it[firstName] = user.firstName
            it[username] = user.userName
            it[languageCode] = user.languageCode
            it[joined] = Instant.now(Clock.systemUTC())
            it[lastUsage] = Instant.now(Clock.systemUTC())
        }.insertedCount > 0
    }

    suspend fun update(user: User) = dbQuery {
        UsersTable.update({ UsersTable.userId eq user.id }) {
            it[firstName] = user.firstName
            it[username] = user.userName
            it[languageCode] = user.languageCode
            it[lastUsage] = Instant.now(Clock.systemUTC())
        }
    }

    suspend fun getLanguageCode(userId: Long): String? = dbQuery {
        UsersTable
            .select(languageCode)
            .where { UsersTable.userId eq userId }
            .singleOrNull()?.get(languageCode)
    }
}

val usersDao = UsersDao()
