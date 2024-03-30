package com.helltar.twitchviewerbot

import com.helltar.twitchviewerbot.Config.DIR_LOCALE
import com.helltar.twitchviewerbot.dao.DatabaseFactory.usersTable
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader
import java.util.regex.Pattern

object Strings {

    const val ADD_COMMAND_INFO = "add_command_info"
    const val CHANNEL_ADDED_TO_LIST = "channel_added_to_list"
    const val CHANNEL_ALREADY_EXISTS_IN_LIST = "channel_already_exists_in_list"
    const val CLIP_COMMAND_INFO = "clip_command_info"
    const val DONT_TOUCH_IS_NOT_YOUR_LIST = "dont_touch_is_not_your_list"
    const val EMPTY_ONLINE_LIST = "empty_online_list"
    const val GET_CLIP_FAIL = "get_clip_fail"
    const val INVALID_CHANNEL_NAME = "invalid_channel_name"
    const val INVALID_CHANNEL_NAME_LENGTH = "invalid_channel_name_length"
    const val LIST_FULL = "list_full"
    const val LIST_IS_EMPTY = "list_is_empty"
    const val LIVE_COMMAND_INFO = "live_command_info"
    const val MANY_REQUEST = "many_request"
    const val SCREENSHOT_COMMAND_INFO = "screenshot_command_info"
    const val START_COMMAND_INFO = "start_command_info"
    const val START_GET_CLIP = "start_get_clip"
    const val STREAM_OFFLINE = "stream_offline"
    const val STREAM_START_TIME = "stream_start_time"
    const val TWITCH_EXCEPTION = "twitch_exception"
    const val USER_CLOSE_LIST = "user_close_list"
    const val WAIT_CHECK_ONLINE = "wait_check_online"
    const val WAIT_CHECK_ONLINE_MENU = "wait_check_online_menu"
    const val WAIT_CHECK_USER_ONLINE = "wait_check_user_online"
    const val WAIT_GET_SCREENSHOT = "wait_get_screenshot"

    const val BTN_BACK = "btn_back"
    const val BTN_CLOSE_LIST = "btn_close_list"
    const val BTN_DELETE = "btn_delete"
    const val BTN_EXIT = "btn_exit"
    const val BTN_GET_ALL_SCREENS = "btn_get_all_screens"
    const val BTN_SCREENSHOT = "btn_screenshot"
    const val BTN_SHORT_CLIP = "btn_short_clip"
    const val BTN_WHO_IS_ONLINE = "btn_who_is_online"
    const val TITLE_CHANNEL_IS_SELECTED = "title_channel_is_selected"
    const val TITLE_CHOOSE_CHANNEL_OR_ACTION = "title_choose_channel_or_action"

    private val log = LoggerFactory.getLogger(javaClass)

    fun localizedString(key: String, userId: Long): String {
        return try {
            val languageCode = usersTable.getLanguageCode(userId)
            var filename = "$DIR_LOCALE/$languageCode.xml"

            if (!File(filename).exists())
                filename = "$DIR_LOCALE/en.xml"

            val regex = """<string name="$key">(\X*?)<\/string>"""
            getFirstRegexGroup(FileReader(filename).readText(), regex).trimIndent().ifEmpty { key }
        } catch (e: Exception) {
            log.error(e.message, e)
            key
        }
    }

    private fun getFirstRegexGroup(text: String, regex: String): String {
        val m = Pattern.compile(regex).matcher(text)

        return if (m.find()) {
            if (m.groupCount() >= 1) m.group(1) else ""
        } else ""
    }
}