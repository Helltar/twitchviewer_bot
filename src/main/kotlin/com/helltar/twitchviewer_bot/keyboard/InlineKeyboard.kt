package com.helltar.twitchviewer_bot.keyboard

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.commands.TwitchCommand
import com.helltar.twitchviewer_bot.commands.commands.ClipCommand
import com.helltar.twitchviewer_bot.commands.commands.LiveCommand
import com.helltar.twitchviewer_bot.commands.commands.ScreenCommand
import com.helltar.twitchviewer_bot.db.Databases
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonChannel
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonClips
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonCloseList
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonLive
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.getChannelNameFromCbData
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.getChannelStatusFromCbData
import com.helltar.twitchviewer_bot.localizedString
import com.helltar.twitchviewer_bot.twitch.Twitch
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.io.Serializable

class InlineKeyboard(val ctx: CallbackQueryContext, private val ownerId: Long) {

    private val twitchCommand: TwitchCommand
    private val liveCommand: LiveCommand
    private val clipCommand: ClipCommand
    private val twitchChannel: String

    init {
        //ctx.update().message = ctx.message()
        val context = MessageContext(ctx.sender, ctx.update(), "")
        twitchCommand = TwitchCommand(context)
        liveCommand = LiveCommand(context)
        clipCommand = ClipCommand(context)
        twitchChannel = getChannelNameFromCbData(ctx.data())
    }

    fun btnClose(ctx: CallbackQueryContext): Serializable =
        editMessage(ctx, String.format(localizedString(Strings.user_close_list, ownerId), ctx.user().firstName))

    fun btnDeleteChannel(ctx: CallbackQueryContext) {
        removeChannelFromUserList(twitchChannel, ownerId)
        update(ctx)
    }

    fun btnScreenshot(ctx: CallbackQueryContext) =
        ScreenCommand(MessageContext(ctx.sender, ctx.update(), "")).getScreenshot(twitchChannel)

    fun btnLive() =
        liveCommand.run { sendOnlineList(getUserChannelsList(ownerId)) }

    fun btnShow() =
        liveCommand.sendOnlineList(listOf(twitchChannel))

    fun btnClip() =
        clipCommand.getClipsFromAll(listOf(twitchChannel))

    fun btnClips() =
        clipCommand.getClipsFromAll(twitchCommand.getUserChannelsList(ownerId))

    fun btnChannel(ctx: CallbackQueryContext) {
        val isChannelLive = getChannelStatusFromCbData(ctx.data())

        if (!isChannelExistsInList(twitchChannel)) {
            update(ctx)
            return
        }

        val inlineKeyboardMarkup = InlineKeyboardMarkup.builder()

        if (isChannelLive) {
            inlineKeyboardMarkup.keyboardRow(
                listOf(
                    createButton(
                        localizedString(Strings.btn_screenshot),
                        setCallbackData(BtnCallbacks.CallbackData(BtnCallbacks.buttonScreenshot, ownerId, twitchChannel))
                    )
                )
            )

            inlineKeyboardMarkup.keyboardRow(
                listOf(
                    createButton(
                        localizedString(Strings.btn_short_clip),
                        setCallbackData(BtnCallbacks.CallbackData(BtnCallbacks.buttonClip, ownerId, twitchChannel))
                    )
                )
            )
        }

        inlineKeyboardMarkup.keyboardRow(
            listOf(
                createButton(
                    localizedString(Strings.btn_back),
                    setCallbackData(BtnCallbacks.CallbackData(BtnCallbacks.buttonBack, ownerId, twitchChannel))
                ),
                createButton(
                    localizedString(Strings.btn_exit),
                    setCallbackData(BtnCallbacks.CallbackData(buttonCloseList, ownerId, twitchChannel))
                ),
                createButton(
                    localizedString(Strings.btn_delete),
                    setCallbackData(BtnCallbacks.CallbackData(BtnCallbacks.buttonDeleteChannel, ownerId, twitchChannel))
                )
            )
        )

        editMessage(
            ctx, String.format(
                localizedString(Strings.title_channel_is_selected),
                "<b><a href=\"https://www.twitch.tv/$twitchChannel\">$twitchChannel</a></b>"
            ), inlineKeyboardMarkup.build()
        )
    }

    private fun isChannelExistsInList(channelName: String) =
        twitchCommand.getUserChannelsList(ownerId).contains(channelName)

    private fun editMessage(ctx: CallbackQueryContext, text: String, replyMarkup: InlineKeyboardMarkup? = null) =
        ctx.editMessage(text, replyMarkup)
            .setParseMode(ParseMode.HTML)
            .disableWebPagePreview()
            .call(ctx.sender)

    fun update(ctx: CallbackQueryContext) {
        if (twitchCommand.getUserChannelsList(ownerId).isNotEmpty()) {
            editMessage(ctx, localizedString(Strings.wait_check_online_menu), initWaitMenu())
            editMessage(ctx, localizedString(Strings.title_choose_channel_or_action), init())
        } else
            editMessage(ctx, localizedString(Strings.list_is_empty))
    }

    private fun createButton(text: String, callbackData: BtnCallbacks.CallbackData) =
        InlineKeyboardButton
            .builder()
            .text(text)
            .callbackData(setCallbackData(listOf(callbackData)))
            .build()

    private fun setCallbackData(data: List<BtnCallbacks.CallbackData>): String = data[0].run {
        return "$btnActName $ownerId $channelName $isChannelLive"
    }

    private fun setCallbackData(callbackData: BtnCallbacks.CallbackData) =
        BtnCallbacks.CallbackData(
            callbackData.btnActName, callbackData.ownerId,
            callbackData.channelName, callbackData.isChannelLive
        )

    fun initWaitMenu(): InlineKeyboardMarkup =
        InlineKeyboardMarkup.builder().keyboardRow(
            createButtonAsList( // 🔄
                "\uD83D\uDD04", setCallbackData(BtnCallbacks.CallbackData(BtnCallbacks.buttonUpdate, ownerId))
            )
        ).build()

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
                        BtnCallbacks.CallbackData(buttonChannel, ownerId, channelName, isLive)
                    )
                )
            else
                secondRowButtons.add(
                    createButton(
                        "$channelStatus $channelName",
                        BtnCallbacks.CallbackData(buttonChannel, ownerId, channelName, isLive)
                    )
                )
        }

        var btnShowLive = listOf<InlineKeyboardButton>()
        var btnClips = listOf<InlineKeyboardButton>()

        if (liveChannels.isNotEmpty()) {
            btnShowLive = createButtonAsList(localizedString(Strings.btn_who_is_online), BtnCallbacks.CallbackData(buttonLive, ownerId))
            btnClips = createButtonAsList(localizedString(Strings.btn_get_all_screens), BtnCallbacks.CallbackData(buttonClips, ownerId))
        }

        return InlineKeyboardMarkup.builder()
            .keyboardRow(firstRowButtons)
            .keyboardRow(secondRowButtons)
            .keyboardRow(btnShowLive)
            .keyboardRow(btnClips)
            .keyboardRow(createButtonAsList(localizedString(Strings.btn_close_list), BtnCallbacks.CallbackData(buttonCloseList, ownerId)))
            .build()
    }

    private fun createButtonAsList(text: String, callbackData: BtnCallbacks.CallbackData) =
        listOf(createButton(text, callbackData))

    private fun removeChannelFromUserList(channelName: String, userId: Long) =
        Databases.dbUserChannels.delete(userId, channelName)

    private fun localizedString(key: String): String {
        return localizedString(key, ownerId)
    }
}
