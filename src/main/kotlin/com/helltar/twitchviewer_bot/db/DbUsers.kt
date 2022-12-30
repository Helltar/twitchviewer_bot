package com.helltar.twitchviewer_bot.db

import com.github.kotlintelegrambot.entities.User
import com.helltar.twitchviewer_bot.db.Databases.dbQuery
import com.helltar.twitchviewer_bot.db.Users.languageCode
import org.jetbrains.exposed.sql.*

class DbUsers {

    private val defaultLanguageCode = "en"

    fun saveUserData(user: User) = dbQuery {
        Users.insertIgnore {
            it[userId] = user.id
            it[firstName] = user.firstName
            it[username] = user.username ?: "null"
            it[languageCode] = user.languageCode ?: defaultLanguageCode
        }

        Users.update({ Users.userId eq user.id }) {
            it[firstName] = user.firstName
            it[username] = user.username ?: "null"
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
