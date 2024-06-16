package com.helltar.twitchviewerbot.dao

import com.helltar.twitchviewerbot.EnvConfig.databaseName
import com.helltar.twitchviewerbot.EnvConfig.databasePassword
import com.helltar.twitchviewerbot.EnvConfig.databaseUser
import com.helltar.twitchviewerbot.EnvConfig.postgresqlHost
import com.helltar.twitchviewerbot.EnvConfig.postgresqlPort
import com.helltar.twitchviewerbot.dao.tables.UserChannelsTable
import com.helltar.twitchviewerbot.dao.tables.UsersTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    val usersDAO = UsersDAO()
    val userChannelsDAO = UserChannelsDAO()

    fun init() {
        val driverClassName = "org.postgresql.Driver"
        val jdbcURL = "jdbc:postgresql://$postgresqlHost:$postgresqlPort/$databaseName"
        val database = Database.connect(jdbcURL, driverClassName, databaseUser, databasePassword)

        transaction(database) {
            SchemaUtils.create(UsersTable, UserChannelsTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}