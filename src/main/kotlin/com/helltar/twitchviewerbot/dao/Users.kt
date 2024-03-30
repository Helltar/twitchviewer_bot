package com.helltar.twitchviewerbot.dao

import com.helltar.twitchviewerbot.dao.DatabaseFactory.dbQuery
import com.helltar.twitchviewerbot.dao.tables.UsersTable
import com.helltar.twitchviewerbot.dao.tables.UsersTable.languageCode
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.telegram.telegrambots.meta.api.objects.User
import java.time.LocalDateTime

class Users {

    fun addUser(user: User) = dbQuery {
        UsersTable.insert {
            it[userId] = user.id
            it[firstName] = user.firstName
            it[username] = user.userName
            it[languageCode] = user.languageCode
            it[joinedDate] = LocalDateTime.now()
            it[lastUsageDate] = LocalDateTime.now()
        }
    }

    fun updateUserData(user: User) = dbQuery {
        UsersTable.update({ UsersTable.userId eq user.id }) {
            it[firstName] = user.firstName
            it[username] = user.userName
            it[languageCode] = user.languageCode
            it[lastUsageDate] = LocalDateTime.now()
        }
    }

    fun getLanguageCode(userId: Long): String? = dbQuery {
        UsersTable.select { UsersTable.userId eq userId }.singleOrNull()?.get(languageCode)
    }

    fun isUserExists(userId: Long) = dbQuery {
        UsersTable.select { UsersTable.userId eq userId }.count() > 0
    }
}