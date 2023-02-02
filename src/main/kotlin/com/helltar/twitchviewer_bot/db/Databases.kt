package com.helltar.twitchviewer_bot.db

import com.helltar.twitchviewer_bot.BotConfig.FILE_DB_USERS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object Databases {

    val dbUsers = DbUsers()
    val dbUserChannels = DbUserChannels()

    fun init() {
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
