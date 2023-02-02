package com.helltar.twitchviewer_bot.db

import com.helltar.twitchviewer_bot.db.Databases.dbQuery
import com.helltar.twitchviewer_bot.db.Users.languageCode
import org.jetbrains.exposed.sql.*
import org.telegram.telegrambots.meta.api.objects.User

class DbUsers {

    private val defaultLanguageCode = "en"

    fun saveUserData(user: User) = dbQuery {
        Users.insertIgnore {
            it[userId] = user.id
            it[firstName] = user.firstName
            it[username] = user.userName ?: "null"
            it[languageCode] = user.languageCode ?: defaultLanguageCode
        }

        Users.update({ Users.userId eq user.id }) {
            it[firstName] = user.firstName
            it[username] = user.userName ?: "null"
            it[languageCode] = user.languageCode ?: defaultLanguageCode
        }
    }

    fun getLanguageCode(userId: Long): String = dbQuery {
        Users.select { Users.userId eq userId }.run {
            if (!empty())
                single()[languageCode]
            else
                defaultLanguageCode
        }
    }
}
