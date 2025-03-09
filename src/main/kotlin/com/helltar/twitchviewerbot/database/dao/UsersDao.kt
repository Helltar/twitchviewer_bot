package com.helltar.twitchviewerbot.database.dao

import com.helltar.twitchviewerbot.database.Database.dbTransaction
import com.helltar.twitchviewerbot.database.Database.utcNow
import com.helltar.twitchviewerbot.database.tables.UsersTable
import com.helltar.twitchviewerbot.database.tables.UsersTable.languageCode
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.update
import org.telegram.telegrambots.meta.api.objects.User

class UsersDao {

    suspend fun add(user: User): Boolean = dbTransaction {
        UsersTable
            .insertIgnore {
                it[userId] = user.id
                it[firstName] = user.firstName
                it[username] = user.userName
                it[languageCode] = user.languageCode
                it[createdAt] = utcNow()
            }
            .insertedCount > 0
    }

    suspend fun update(user: User): Boolean = dbTransaction {
        UsersTable
            .update({ UsersTable.userId eq user.id }) {
                it[firstName] = user.firstName
                it[username] = user.userName
                it[languageCode] = user.languageCode
                it[updatedAt] = utcNow()
            } > 0
    }

    suspend fun languageCode(userId: Long): String? = dbTransaction {
        UsersTable
            .select(languageCode)
            .where { UsersTable.userId eq userId }
            .singleOrNull()?.get(languageCode)
    }
}

val usersDao = UsersDao()
