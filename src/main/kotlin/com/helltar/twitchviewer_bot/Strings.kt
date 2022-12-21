package com.helltar.twitchviewer_bot

import com.helltar.twitchviewer_bot.utils.Utils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.util.*

object Strings {

    const val add_command_info = "add_command_info"
    const val channel_added_to_list = "channel_added_to_list"
    const val channel_already_exists_in_list = "channel_already_exists_in_list"
    const val clip_command_info = "clip_command_info"
    const val dont_touch_is_not_your_list = "dont_touch_is_not_your_list"
    const val empty_online_list = "empty_online_list"
    const val get_clip_fail = "get_clip_fail"
    const val invalid_channel_name = "invalid_channel_name"
    const val invalid_channel_name_length = "invalid_channel_name_length"
    const val list_full = "list_full"
    const val list_is_empty = "list_is_empty"
    const val live_command_info = "live_command_info"
    const val many_request = "many_request"
    const val screenshot_command_info = "screenshot_command_info"
    const val start_get_clip = "start_get_clip"
    const val start_command_info = "start_command_info"
    const val stream_offline = "stream_offline"
    const val stream_start_time = "stream_start_time"
    const val twitch_exception = "twitch_exception"
    const val user_close_list = "user_close_list"
    const val wait_check_online = "wait_check_online"
    const val wait_check_online_menu = "wait_check_online_menu"
    const val wait_check_user_online = "wait_check_user_online"
    const val wait_get_screenshot = "wait_get_screenshot"

    const val btn_back = "btn_back"
    const val btn_close_list = "btn_close_list"
    const val btn_delete = "btn_delete"
    const val btn_exit = "btn_exit"
    const val btn_get_all_screens = "btn_get_all_screens"
    const val btn_screenshot = "btn_screenshot"
    const val btn_short_clip = "btn_short_clip"
    const val btn_who_is_online = "btn_who_is_online"
    const val title_channel_is_selected = "title_channel_is_selected"
    const val title_choose_channel_or_action = "title_choose_channel_or_action"
}

private const val DEFAULT_LANGUAGE_CODE = "en"
private var userLanguageCode = DEFAULT_LANGUAGE_CODE

private fun setUserLanguage(languageCode: String?) {
    userLanguageCode = languageCode ?: DEFAULT_LANGUAGE_CODE
}

fun localizedString(key: String, userId: Long): String {
    try {
        if (File(BotConfig.DIR_DB_USER + userId).exists())
            Properties().run {
                load(FileInputStream(BotConfig.DIR_DB_USER + userId))
                setUserLanguage(getProperty(User.PROP_KEY_USER_LANGUAGECODE).ifEmpty { null })
            }

        var filename = BotConfig.DIR_LOCALE + userLanguageCode + BotConfig.EXT_XML

        if (!File(filename).exists())
            filename = BotConfig.DIR_LOCALE + DEFAULT_LANGUAGE_CODE + BotConfig.EXT_XML

        /* todo. locale. lib. */

        val regex = """<string name="$key">(\X*?)<\/string>"""

        return Utils.getFirstRegexGroup(FileReader(filename).readText(), regex).trimIndent().ifEmpty { key }
    } catch (e: Exception) {
        LoggerFactory.getLogger(Strings.javaClass).error(e.message)
        return key
    }
}
