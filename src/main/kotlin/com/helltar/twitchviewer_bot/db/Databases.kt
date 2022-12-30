package com.helltar.twitchviewer_bot.db

import com.helltar.twitchviewer_bot.BotConfig.DIR_DB
import com.helltar.twitchviewer_bot.BotConfig.FILE_DB_USERS
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import org.jetbrains.exposed.sql.transactions.experimental.*
import java.io.File

object Databases {

    val dbUsers = DbUsers()
    val dbUserChannels = DbUserChannels()

    fun init() {
        if (!File(DIR_DB).exists()) File(DIR_DB).mkdir()

        val driver = "org.sqlite.JDBC"
        val url = "jdbc:sqlite:$FILE_DB_USERS"

        val database = Database.connect(url, driver)

        transaction(database) {
            SchemaUtils.create(Users, UserChannels)
        }
    }

    fun <T> dbQuery(block: () -> T): T =
        runBlocking {
            newSuspendedTransaction(Dispatchers.IO) {
                addLogger(StdOutSqlLogger)
                block()
            }
        }
}
