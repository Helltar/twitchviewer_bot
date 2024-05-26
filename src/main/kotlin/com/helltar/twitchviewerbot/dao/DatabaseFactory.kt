package com.helltar.twitchviewerbot.dao

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
import java.io.File

object DatabaseFactory {

    val usersDAO = UsersDAO()
    val userChannelDAO = UserChannelDAO()

    private const val DIR_DB = "data/database"
    private const val DATABASE_FILE = "./$DIR_DB/database"

    fun init() {
        val databaseDir = File(DIR_DB)

        if (!databaseDir.exists() && !databaseDir.mkdirs())
            throw RuntimeException("error when create dir: $DIR_DB")

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