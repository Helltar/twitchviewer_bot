package com.helltar.twitchviewerbot.dao

import com.helltar.twitchviewerbot.Config.DATABASE_FILE
import com.helltar.twitchviewerbot.dao.tables.UsersChannelsTable
import com.helltar.twitchviewerbot.dao.tables.UsersTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    val usersTable = Users()
    val userChannelsTable = UserChannels()

    fun init() {
        val driver = "org.h2.Driver"
        val url = "jdbc:h2:file:$DATABASE_FILE"

        val database = Database.connect(url, driver)

        transaction(database) {
            SchemaUtils.create(UsersTable, UsersChannelsTable)
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