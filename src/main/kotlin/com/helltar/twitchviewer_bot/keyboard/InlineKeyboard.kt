package com.helltar.twitchviewer_bot.keyboard

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.commands.TwitchCommand
import com.helltar.twitchviewer_bot.commands.commands.ClipCommand
import com.helltar.twitchviewer_bot.commands.commands.LiveCommand
import com.helltar.twitchviewer_bot.commands.commands.ScreenCommand
import com.helltar.twitchviewer_bot.db.Databases.dbUserChannels
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.CallbackData
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonBack
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonChannel
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonClip
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonClips
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonCloseList
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonDeleteChannel
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonLive
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonScreenshot
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonUpdate
import com.helltar.twitchviewer_bot.localizedString
import com.helltar.twitchviewer_bot.twitch.Twitch

class InlineKeyboard(private val bot: Bot, private val message: Message, private val ownerId: Long) {

    private val chatId = ChatId.fromId(message.chat.id)
    private val twitchCommand = TwitchCommand(bot, message)
    private val liveCommand = LiveCommand(bot, message)
    private val clipCommand = ClipCommand(bot, message)

    fun btnShow(twitchChannel: String) =
        liveCommand.sendOnlineList(listOf(twitchChannel))

    fun btnLive() =
        liveCommand.run { sendOnlineList(getUserChannelsList(ownerId)) }

    fun btnClip(twitchChannel: String) =
        clipCommand.getClipsFromAll(listOf(twitchChannel))

    fun btnClips() =
        clipCommand.getClipsFromAll(twitchCommand.getUserChannelsList(ownerId))

    fun btnDeleteChannel(twitchChannel: String) {
        removeChannelFromUserList(twitchChannel, ownerId)
        update()
    }

    fun btnScreenshot(twitchChannel: String) =
        ScreenCommand(bot, message).getScreenshot(twitchChannel)

    fun btnChannel(twitchChannel: String, isChannelLive: Boolean) {
        if (!isChannelExistsInList(twitchChannel)) {
            update()
            return
        }

        var btnScreenshot = listOf<InlineKeyboardButton>()
        var btnClip = listOf<InlineKeyboardButton>()

        if (isChannelLive) {
            btnScreenshot = createButtonAsList(localizedString(Strings.btn_screenshot), CallbackData(buttonScreenshot, ownerId, twitchChannel))
            btnClip = createButtonAsList(localizedString(Strings.btn_short_clip), CallbackData(buttonClip, ownerId, twitchChannel))
        }

        val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
            btnScreenshot, btnClip,
            listOf(
                createButton(localizedString(Strings.btn_back), CallbackData(buttonBack, ownerId, twitchChannel)),
                createButton(localizedString(Strings.btn_exit), CallbackData(buttonCloseList, ownerId, twitchChannel)),
                createButton(localizedString(Strings.btn_delete), CallbackData(buttonDeleteChannel, ownerId, twitchChannel))
            )
        )

        editMessageText(
            String.format(
                localizedString(Strings.title_channel_is_selected),
                "<b><a href=\"https://www.twitch.tv/$twitchChannel\">$twitchChannel</a></b>"
            ), inlineKeyboardMarkup
        )
    }

    fun initWaitMenu(): InlineKeyboardMarkup =
        InlineKeyboardMarkup.create(createButtonAsList("\uD83D\uDD04", CallbackData(buttonUpdate, ownerId))) // 🔄

    fun init(): InlineKeyboardMarkup {
        val firstRowButtons = arrayListOf<InlineKeyboardButton>()
        val secondRowButtons = arrayListOf<InlineKeyboardButton>()

        val userChannels = twitchCommand.getUserChannelsList(ownerId)
        val twitchOnlineList = Twitch().getOnlineList(userChannels) ?: listOf()
        val liveChannels = arrayListOf<String>()

        twitchOnlineList.forEach {
            liveChannels.add(it.login.lowercase())
        }

        userChannels.forEachIndexed { i, channelName ->
            var channelStatus = "⚪️"
            var isLive = 0

            if (liveChannels.contains(channelName.lowercase())) {
                channelStatus = "\uD83D\uDD34" // 🔴
                isLive = 1
            }

            if (i <= 1)
                firstRowButtons.add(
                    createButton(
                        "$channelStatus $channelName",
                        CallbackData(buttonChannel, ownerId, channelName, isLive)
                    )
                )
            else
                secondRowButtons.add(
                    createButton(
                        "$channelStatus $channelName",
                        CallbackData(buttonChannel, ownerId, channelName, isLive)
                    )
                )
        }

        var btnShowLive = listOf<InlineKeyboardButton>()
        var btnClips = listOf<InlineKeyboardButton>()

        if (liveChannels.isNotEmpty()) {
            btnShowLive = createButtonAsList(localizedString(Strings.btn_who_is_online), CallbackData(buttonLive, ownerId))
            btnClips = createButtonAsList(localizedString(Strings.btn_get_all_screens), CallbackData(buttonClips, ownerId))
        }

        return InlineKeyboardMarkup.create(
            firstRowButtons, secondRowButtons, btnShowLive, btnClips,
            createButtonAsList(localizedString(Strings.btn_close_list), CallbackData(buttonCloseList, ownerId))
        )
    }

    fun update() {
        if (twitchCommand.getUserChannelsList(ownerId).isNotEmpty()) {
            editMessageText(localizedString(Strings.wait_check_online_menu), replyMarkup = initWaitMenu())
            editMessageText(localizedString(Strings.title_choose_channel_or_action), replyMarkup = init())
        } else
            editMessageText(localizedString(Strings.list_is_empty))
    }

    private fun createButton(text: String, callbackData: CallbackData) =
        InlineKeyboardButton.CallbackData(text, setCallbackData(listOf(callbackData)))

    private fun createButtonAsList(text: String, callbackData: CallbackData) =
        listOf(createButton(text, callbackData))

    private fun editMessageText(text: String, replyMarkup: ReplyMarkup? = null) {
        bot.editMessageText(
            chatId, message.messageId,
            text = text, parseMode = ParseMode.HTML,
            disableWebPagePreview = true, replyMarkup = replyMarkup
        )
    }

    private fun removeChannelFromUserList(channelName: String, userId: Long) =
        dbUserChannels.delete(userId, channelName)

    private fun isChannelExistsInList(channelName: String) =
        twitchCommand.getUserChannelsList(ownerId).contains(channelName)

    private fun localizedString(key: String): String {
        return localizedString(key, ownerId)
    }

    private fun setCallbackData(data: List<CallbackData>): String = data[0].run {
        return "$btnActName $ownerId $channelName $isChannelLive"
    }
}
