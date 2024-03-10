package com.helltar.twitchviewerbot.keyboard

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.commands.ClipCommand
import com.helltar.twitchviewerbot.commands.commands.LiveCommand
import com.helltar.twitchviewerbot.commands.commands.ScreenCommand
import com.helltar.twitchviewerbot.dao.DatabaseFactory.userChannels
import com.helltar.twitchviewerbot.keyboard.BtnCallbacks.BUTTON_CHANNEL
import com.helltar.twitchviewerbot.keyboard.BtnCallbacks.BUTTON_CLIPS
import com.helltar.twitchviewerbot.keyboard.BtnCallbacks.BUTTON_CLOSE_LIST
import com.helltar.twitchviewerbot.keyboard.BtnCallbacks.BUTTON_LIVE
import com.helltar.twitchviewerbot.keyboard.BtnCallbacks.BUTTON_UPDATE
import com.helltar.twitchviewerbot.keyboard.BtnCallbacks.getChannelName
import com.helltar.twitchviewerbot.keyboard.BtnCallbacks.getChannelStatus
import com.helltar.twitchviewerbot.twitch.Twitch
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.io.Serializable

/* todo: refact. ._. */

class InlineKeyboard(val ctx: CallbackQueryContext, private val ownerId: Long) {

    private val liveCommand: LiveCommand
    private val clipCommand: ClipCommand
    private val twitchChannel: String

    init {
        val context = MessageContext(ctx.sender, ctx.update(), "")
        liveCommand = LiveCommand(context)
        clipCommand = ClipCommand(context)
        twitchChannel = getChannelName(ctx.data())
    }

    fun btnClose(ctx: CallbackQueryContext): Serializable =
        editMessage(ctx, String.format(localizedString(Strings.user_close_list), ctx.user().firstName))

    fun btnDeleteChannel(ctx: CallbackQueryContext) {
        removeChannelFromUserList(twitchChannel, ownerId)
        update(ctx)
    }

    fun btnScreenshot(ctx: CallbackQueryContext) =
        ScreenCommand(MessageContext(ctx.sender, ctx.update(), "")).getScreenshot(twitchChannel)

    fun btnLive() =
        liveCommand.run { sendOnlineList(userChannels.getUserChannelsList(ownerId)) }

    fun btnShow() =
        liveCommand.sendOnlineList(listOf(twitchChannel))

    fun btnClip() =
        clipCommand.getClipsFromAll(listOf(twitchChannel))

    fun btnClips() =
        clipCommand.getClipsFromAll(userChannels.getUserChannelsList(ownerId))

    fun btnChannel(ctx: CallbackQueryContext) {
        val isChannelLive = getChannelStatus(ctx.data())

        if (userChannels.isChannelNotExists(ownerId, twitchChannel)) {
            update(ctx)
            return
        }

        val inlineKeyboardMarkup = InlineKeyboardMarkup.builder()

        if (isChannelLive) {
            inlineKeyboardMarkup.keyboardRow(
                listOf(
                    createButton(
                        localizedString(Strings.btn_screenshot),
                        BtnCallbacks.CallbackData(BtnCallbacks.BUTTON_SCREENSHOT, ownerId, twitchChannel)
                    )
                )
            )

            inlineKeyboardMarkup.keyboardRow(
                listOf(
                    createButton(
                        localizedString(Strings.btn_short_clip),
                        BtnCallbacks.CallbackData(BtnCallbacks.BUTTON_CLIP, ownerId, twitchChannel)
                    )
                )
            )
        }

        inlineKeyboardMarkup.keyboardRow(
            listOf(
                createButton(
                    localizedString(Strings.btn_back),
                    BtnCallbacks.CallbackData(BtnCallbacks.BUTTON_BACK, ownerId, twitchChannel)
                ),
                createButton(
                    localizedString(Strings.btn_exit),
                    BtnCallbacks.CallbackData(BUTTON_CLOSE_LIST, ownerId, twitchChannel)
                ),
                createButton(
                    localizedString(Strings.btn_delete),
                    BtnCallbacks.CallbackData(BtnCallbacks.BUTTON_DELETE_CHANNEL, ownerId, twitchChannel)
                )
            )
        )

        editMessage(
            ctx,
            localizedString(Strings.title_channel_is_selected).format("<b><a href=\"https://www.twitch.tv/$twitchChannel\">$twitchChannel</a></b>"),
            inlineKeyboardMarkup.build()
        )
    }

    private fun editMessage(ctx: CallbackQueryContext, text: String, replyMarkup: InlineKeyboardMarkup? = null) =
        ctx.editMessage(text, replyMarkup)
            .setParseMode(ParseMode.HTML)
            .disableWebPagePreview()
            .call(ctx.sender)

    fun update(ctx: CallbackQueryContext) {
        if (userChannels.getUserChannelsList(ownerId).isNotEmpty()) {
            editMessage(ctx, localizedString(Strings.wait_check_online_menu), initWaitMenu())
            editMessage(ctx, localizedString(Strings.title_choose_channel_or_action), init())
        } else
            editMessage(ctx, localizedString(Strings.list_is_empty))
    }

    private fun createButton(text: String, callbackData: BtnCallbacks.CallbackData) =
        InlineKeyboardButton
            .builder()
            .text(text)
            .callbackData(callbackDataToString(callbackData))
            .build()

    private fun callbackDataToString(callbackData: BtnCallbacks.CallbackData) =
        callbackData.run { "$btnActName $ownerId $channelName $isChannelLive" }

    fun initWaitMenu(): InlineKeyboardMarkup =
        InlineKeyboardMarkup
            .builder()
            .keyboardRow(createButtonAsList("\uD83D\uDD04", BUTTON_UPDATE, ownerId)) // 🔄
            .build()

    fun init(): InlineKeyboardMarkup {
        val keyboardRow = arrayListOf<InlineKeyboardButton>()
        val userChannels = userChannels.getUserChannelsList(ownerId)
        val twitchOnlineList = Twitch().getOnlineList(userChannels) ?: listOf()
        val liveChannels = twitchOnlineList.map { it.login.lowercase() }

        userChannels.forEach { channelName ->
            var channelStatus = "⚪️"
            var isLive = 0

            if (channelName.lowercase() in liveChannels) {
                channelStatus = "\uD83D\uDD34" // 🔴
                isLive = 1
            }

            keyboardRow.add(
                createButton(
                    "$channelStatus $channelName",
                    BtnCallbacks.CallbackData(BUTTON_CHANNEL, ownerId, channelName, isLive)
                )
            )
        }

        var btnShowLive = listOf<InlineKeyboardButton>()
        var btnClips = listOf<InlineKeyboardButton>()

        if (liveChannels.isNotEmpty()) {
            btnShowLive = createButtonAsList(localizedString(Strings.btn_who_is_online), BUTTON_LIVE, ownerId)
            btnClips = createButtonAsList(localizedString(Strings.btn_get_all_screens), BUTTON_CLIPS, ownerId)
        }

        val keyboardMarkupBuilder = InlineKeyboardMarkup.builder()

        keyboardRow.chunked(2).forEach {
            keyboardMarkupBuilder.keyboardRow(it)
        }

        return keyboardMarkupBuilder
            .keyboardRow(btnShowLive)
            .keyboardRow(btnClips)
            .keyboardRow(createButtonAsList(localizedString(Strings.btn_close_list), BUTTON_CLOSE_LIST, ownerId))
            .build()
    }

    private fun createButtonAsList(text: String, btnName: String, ownerId: Long) =
        listOf(createButton(text, BtnCallbacks.CallbackData(btnName, ownerId)))

    private fun removeChannelFromUserList(channelName: String, userId: Long) =
        userChannels.delete(userId, channelName)

    private fun localizedString(key: String) =
        Strings.localizedString(key, ownerId)
}