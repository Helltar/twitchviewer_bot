package com.helltar.twitchviewerbot.database

import com.helltar.twitchviewerbot.Config.databaseName
import com.helltar.twitchviewerbot.Config.databasePassword
import com.helltar.twitchviewerbot.Config.databaseUser
import com.helltar.twitchviewerbot.Config.postgresqlHost
import com.helltar.twitchviewerbot.Config.postgresqlPort
import com.helltar.twitchviewerbot.database.tables.UserChannelsTable
import com.helltar.twitchviewerbot.database.tables.UsersTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock
import java.time.Instant

object Database {

    fun init() {
        val driverClassName = "org.postgresql.Driver"
        val jdbcURL = "jdbc:postgresql://$postgresqlHost:$postgresqlPort/$databaseName"
        val database = Database.connect(jdbcURL, driverClassName, databaseUser, databasePassword)

        transaction(database) {
            SchemaUtils.create(UsersTable, UserChannelsTable)
        }
    }

    fun utcNow(): Instant =
        Instant.now(Clock.systemUTC())

    suspend fun <T> dbTransaction(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
