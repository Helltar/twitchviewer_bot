package com.helltar.twitchviewerbot.dao.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object UsersChannelsTable : Table() {

    val userId = long("userId")
    val channel = varchar("channel", 32)
    val datetime = datetime("datetime")
}