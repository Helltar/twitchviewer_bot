package com.helltar.twitchviewer_bot

import com.github.kotlintelegrambot.entities.User
import org.slf4j.LoggerFactory
import java.io.FileOutputStream
import java.util.*

class User {

    companion object {
        private const val PROP_KEY_USER_ID = "user.id"
        private const val PROP_KEY_USER_FIRSTNAME = "user.firstName"
        private const val PROP_KEY_USER_USERNAME = "user.username"
        const val PROP_KEY_USER_LANGUAGECODE = "user.languageCode"
    }

    fun saveUserInfo(user: User) {
        try {
            Properties().run {
                setProperty(PROP_KEY_USER_ID, "${user.id}")
                setProperty(PROP_KEY_USER_FIRSTNAME, user.firstName)
                setProperty(PROP_KEY_USER_USERNAME, "${user.username}")
                setProperty(PROP_KEY_USER_LANGUAGECODE, user.languageCode ?: "")
                store(FileOutputStream(BotConfig.DIR_DB_USER + user.id), null)
            }
        } catch (e: Exception) {
            LoggerFactory.getLogger(javaClass).error(e.message)
        }
    }
}
