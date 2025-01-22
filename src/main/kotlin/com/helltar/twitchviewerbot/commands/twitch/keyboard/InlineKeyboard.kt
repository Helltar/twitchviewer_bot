package com.helltar.twitchviewerbot.commands.twitch.keyboard

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Config
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.Strings.BTN_BACK
import com.helltar.twitchviewerbot.Strings.BTN_DELETE
import com.helltar.twitchviewerbot.Strings.BTN_EXIT
import com.helltar.twitchviewerbot.Strings.BTN_SHORT_CLIP
import com.helltar.twitchviewerbot.Strings.localizedString
import com.helltar.twitchviewerbot.commands.twitch.ClipCommand
import com.helltar.twitchviewerbot.commands.twitch.LiveCommand
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.BUTTON_BACK
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.BUTTON_CHANNEL
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.BUTTON_CLIP
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.BUTTON_CLIPS
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.BUTTON_CLOSE_LIST
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.BUTTON_DELETE_CHANNEL
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.BUTTON_LIVE
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.BUTTON_NEXT_PAGE
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.BUTTON_PREV_PAGE
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.BUTTON_UPDATE
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.parseChannelName
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.parseNavigationPage
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.parseStreamLiveState
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.string
import com.helltar.twitchviewerbot.db.dao.userChannelsDao
import com.helltar.twitchviewerbot.db.dao.usersDao
import com.helltar.twitchviewerbot.twitch.Twitch
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow

class InlineKeyboard(private val ctx: CallbackQueryContext, private val ownerId: Long) {

    private companion object {
        const val CHANNELS_PER_PAGE = 8
        const val CHANNELS_PER_ROW = 2
    }

    private val messageContext = MessageContext(ctx.sender, ctx.update(), String()).apply { this.user().id = ownerId } // todo: user().id

    private val channelName = parseChannelName(ctx.data())
    private val navigationPage = parseNavigationPage(ctx.data())
    private var userLanguageCode: String? = null

    suspend fun channel() {
        val keyboard = InlineKeyboardMarkup.builder()
        val isStreamLive = parseStreamLiveState(ctx.data())

        if (isStreamLive) {
            val buttonClip = keyboardButton(localizedString(BTN_SHORT_CLIP), BUTTON_CLIP, channelName)
            keyboard.keyboardRow(InlineKeyboardRow(buttonClip))
        }

        val buttonBack = keyboardButton(localizedString(BTN_BACK), BUTTON_BACK)
        val buttonClose = keyboardButton(localizedString(BTN_EXIT), BUTTON_CLOSE_LIST)
        val buttonDelete = keyboardButton(localizedString(BTN_DELETE), BUTTON_DELETE_CHANNEL, channelName)

        keyboard.keyboardRow(InlineKeyboardRow(buttonBack, buttonClose, buttonDelete))

        val link = """<b><a href="https://www.twitch.tv/$channelName">$channelName</a></b>"""
        val text = localizedString(Strings.TITLE_CHANNEL_IS_SELECTED).format(link)

        editMessage(text, keyboard.build())
    }

    suspend fun close() {
        val text = localizedString(Strings.USER_CLOSE_LIST).format(ctx.user().firstName, Config.botUsername)
        editMessage(text)
    }

    suspend fun clip() {
        ClipCommand(messageContext).getClip(channelName)
    }

    suspend fun clips() {
        val channels = userChannelsDao.getChannels(ownerId)

        if (channels.isNotEmpty())
            ClipCommand(messageContext).getClipsFromAll(channels)
    }

    suspend fun live() {
        val channels = userChannelsDao.getChannels(ownerId)

        if (channels.isNotEmpty())
            LiveCommand(messageContext).sendOnlineList(channels)
    }

    suspend fun deleteChannel() {
        userChannelsDao.delete(ownerId, channelName)
        update()
    }

    suspend fun update() {
        if (userChannelsDao.isChannelsListNotEmpty(ownerId)) {
            editMessage(localizedString(Strings.WAIT_CHECK_ONLINE_MENU), waitingMenu())
            editMessage(localizedString(Strings.TITLE_CHOOSE_CHANNEL_OR_ACTION), mainMenu())
        } else
            editMessage(localizedString(Strings.LIST_IS_EMPTY))
    }

    suspend fun mainMenu(): InlineKeyboardMarkup {
        val userChannels = userChannelsDao.getChannels(ownerId)
        val onlineList = Twitch().getOnlineList(userChannels) ?: listOf()
        val liveStreams = onlineList.map { it.login.lowercase() }
        val sortedChannels = userChannels.sortedByDescending { it in liveStreams }
        val paginatedChannels = sortedChannels.chunked(CHANNELS_PER_PAGE)
        val safePage = navigationPage.coerceIn(0, paginatedChannels.lastIndex)
        val buttons = mutableListOf<InlineKeyboardButton>()

        paginatedChannels[safePage].forEach { channel ->
            var channelStatus = "⚪️"
            var streamLive = false

            if (channel.lowercase() in liveStreams) {
                channelStatus = "\uD83D\uDD34" // 🔴
                streamLive = true
            }

            buttons.add(keyboardButton("$channelStatus $channel", BUTTON_CHANNEL, channel, streamLive))
        }

        val keyboard = InlineKeyboardMarkup.builder()

        buttons.chunked(CHANNELS_PER_ROW).forEach { button ->
            keyboard.keyboardRow(InlineKeyboardRow(button))
        }

        val navigationRow = mutableListOf<InlineKeyboardButton>()

        if (safePage > 0)
            navigationRow.add(keyboardButton("⬅\uFE0F", BUTTON_PREV_PAGE, page = safePage - 1))

        if (safePage < paginatedChannels.lastIndex)
            navigationRow.add(keyboardButton("➡\uFE0F", BUTTON_NEXT_PAGE, page = safePage + 1))

        if (navigationRow.isNotEmpty())
            keyboard.keyboardRow(InlineKeyboardRow(navigationRow))

        if (liveStreams.isNotEmpty()) {
            val buttonLive = keyboardButton(localizedString(Strings.BTN_WHO_IS_ONLINE), BUTTON_LIVE)
            val buttonClips = keyboardButton(localizedString(Strings.BTN_GET_ALL_SCREENS), BUTTON_CLIPS)
            keyboard.keyboardRow(InlineKeyboardRow(listOf(buttonLive, buttonClips)))
        }

        val buttonClose = keyboardButton(localizedString(Strings.BTN_CLOSE_LIST), BUTTON_CLOSE_LIST)
        keyboard.keyboardRow(InlineKeyboardRow(buttonClose))

        return keyboard.build()
    }

    fun waitingMenu(): InlineKeyboardMarkup {
        val button = keyboardButton("\uD83D\uDD04", BUTTON_UPDATE)
        return InlineKeyboardMarkup.builder().keyboardRow(InlineKeyboardRow(button)).build()
    }

    private fun editMessage(text: String, replyMarkup: InlineKeyboardMarkup? = null) =
        ctx.editMessage(text, replyMarkup)
            .setParseMode(ParseMode.HTML)
            .disableWebPagePreview()
            .call(ctx.sender)

    private fun keyboardButton(text: String, buttonId: String, channelName: String = "-", isStreamLive: Boolean = false, page: Int = navigationPage): InlineKeyboardButton {
        val callbackData = ButtonCallbacks.CallbackData(buttonId, ownerId, channelName, isStreamLive, page)
        return InlineKeyboardButton.builder().text(text).callbackData(callbackData.string()).build()
    }

    private suspend fun localizedString(key: String) =
        localizedString(key, userLanguageCode ?: usersDao.getLanguageCode(ownerId).also { userLanguageCode = it })
}
