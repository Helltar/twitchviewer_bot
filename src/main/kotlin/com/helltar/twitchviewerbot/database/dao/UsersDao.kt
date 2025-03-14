package com.helltar.twitchviewerbot.database.dao

import com.helltar.twitchviewerbot.database.Database.dbTransaction
import com.helltar.twitchviewerbot.database.Database.utcNow
import com.helltar.twitchviewerbot.database.tables.UsersTable
import com.helltar.twitchviewerbot.database.tables.UsersTable.firstName
import com.helltar.twitchviewerbot.database.tables.UsersTable.languageCode
import com.helltar.twitchviewerbot.database.tables.UsersTable.updatedAt
import com.helltar.twitchviewerbot.database.tables.UsersTable.username
import org.jetbrains.exposed.sql.upsert
import org.telegram.telegrambots.meta.api.objects.User

class UsersDao {

    suspend fun upsert(user: User) = dbTransaction {
        UsersTable
            .upsert(
                onUpdate = {
                    it[firstName] = user.firstName
                    it[username] = user.userName
                    it[languageCode] = user.languageCode
                    it[updatedAt] = utcNow()
                })
            {
                it[userId] = user.id
                it[firstName] = user.firstName
                it[username] = user.userName
                it[languageCode] = user.languageCode
                it[createdAt] = utcNow()
            }
    }

    suspend fun languageCode(userId: Long): String? = dbTransaction {
        UsersTable
            .select(languageCode)
            .where { UsersTable.userId eq userId }
            .singleOrNull()?.get(languageCode)
    }
}

val usersDao = UsersDao()
