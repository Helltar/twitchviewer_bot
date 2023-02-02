package com.helltar.twitchviewer_bot.db

import com.helltar.twitchviewer_bot.db.Databases.dbQuery
import com.helltar.twitchviewer_bot.db.UserChannels.channels
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

class DbUserChannels {

    fun isNotEmpty(userId: Long) = getList(userId).isNotEmpty()

    fun getSize(userId: Long) = getList(userId).size

    // todo: ._.
    fun add(userId: Long, channel: String): Boolean {
        arrayListOf<String>().run {
            addAll(getList(userId))

            if (!contains(channel)) {
                if (add(channel)) {
                    addAllFromList(userId, this)
                    return true
                }
            }
        }

        return false
    }

    // todo: ._.
    fun delete(userId: Long, channel: String) {
        arrayListOf<String>().run {
            addAll(getList(userId))

            if (remove(channel))
                addAllFromList(userId, this)
        }
    }

    fun getList(userId: Long): List<String> = dbQuery {
        UserChannels.select { UserChannels.userId eq userId }.run {
            if (!empty())
                single()[channels].run {
                    if (isNotEmpty())
                        return@dbQuery split(" ")
                }
        }

        return@dbQuery listOf()
    }

    private fun addAllFromList(userId: Long, channels: List<String>) = dbQuery {
        UserChannels.insertIgnore {
            it[this.userId] = userId
            it[this.channels] = ""
        }

        UserChannels.update({ UserChannels.userId eq userId }) {
            it[this.channels] = channels.joinToString(" ")
        }
    }
}
