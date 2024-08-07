package com.helltar.twitchviewerbot.db

import com.helltar.twitchviewerbot.EnvConfig.databaseName
import com.helltar.twitchviewerbot.EnvConfig.databasePassword
import com.helltar.twitchviewerbot.EnvConfig.databaseUser
import com.helltar.twitchviewerbot.EnvConfig.postgresqlHost
import com.helltar.twitchviewerbot.EnvConfig.postgresqlPort
import com.helltar.twitchviewerbot.db.tables.PrivacyPoliciesTable
import com.helltar.twitchviewerbot.db.tables.UserChannelsTable
import com.helltar.twitchviewerbot.db.tables.UsersTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        val driverClassName = "org.postgresql.Driver"
        val jdbcURL = "jdbc:postgresql://$postgresqlHost:$postgresqlPort/$databaseName"
        val database = Database.connect(jdbcURL, driverClassName, databaseUser, databasePassword)

        transaction(database) {
            SchemaUtils.create(UsersTable, UserChannelsTable, PrivacyPoliciesTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}