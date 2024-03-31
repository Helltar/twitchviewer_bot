package com.helltar.twitchviewerbot.keyboard

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Config
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.command.commands.ClipCommand
import com.helltar.twitchviewerbot.command.commands.LiveCommand
import com.helltar.twitchviewerbot.command.commands.ScreenCommand
import com.helltar.twitchviewerbot.dao.DatabaseFactory.userChannelsTable
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
        editMessage(ctx, localizedString(Strings.USER_CLOSE_LIST).format(ctx.user().firstName, Config.botUsername))

    fun btnDeleteChannel(ctx: CallbackQueryContext) {
        removeChannelFromUserList(twitchChannel, ownerId)
        update(ctx)
    }

    fun btnScreenshot(ctx: CallbackQueryContext) =
        ScreenCommand(MessageContext(ctx.sender, ctx.update(), "")).getScreenshot(twitchChannel)

    fun btnLive() =
        liveCommand.run { sendOnlineList(userChannelsTable.getUserChannelsList(ownerId)) }

    fun btnShow() =
        liveCommand.sendOnlineList(listOf(twitchChannel))

    fun btnClip() =
        clipCommand.getClipsFromAll(listOf(twitchChannel))

    fun btnClips() =
        clipCommand.getClipsFromAll(userChannelsTable.getUserChannelsList(ownerId))

    fun btnChannel(ctx: CallbackQueryContext) {
        val isChannelLive = getChannelStatus(ctx.data())

        if (userChannelsTable.isChannelNotExists(ownerId, twitchChannel)) {
            update(ctx)
            return
        }

        val inlineKeyboardMarkup = InlineKeyboardMarkup.builder()

        if (isChannelLive) {
            inlineKeyboardMarkup.keyboardRow(
                listOf(
                    createButton(
                        localizedString(Strings.BTN_SCREENSHOT),
                        BtnCallbacks.CallbackData(BtnCallbacks.BUTTON_SCREENSHOT, ownerId, twitchChannel)
                    )
                )
            )

            inlineKeyboardMarkup.keyboardRow(
                listOf(
                    createButton(
                        localizedString(Strings.BTN_SHORT_CLIP),
                        BtnCallbacks.CallbackData(BtnCallbacks.BUTTON_CLIP, ownerId, twitchChannel)
                    )
                )
            )
        }

        inlineKeyboardMarkup.keyboardRow(
            listOf(
                createButton(
                    localizedString(Strings.BTN_BACK),
                    BtnCallbacks.CallbackData(BtnCallbacks.BUTTON_BACK, ownerId, twitchChannel)
                ),
                createButton(
                    localizedString(Strings.BTN_EXIT),
                    BtnCallbacks.CallbackData(BUTTON_CLOSE_LIST, ownerId, twitchChannel)
                ),
                createButton(
                    localizedString(Strings.BTN_DELETE),
                    BtnCallbacks.CallbackData(BtnCallbacks.BUTTON_DELETE_CHANNEL, ownerId, twitchChannel)
                )
            )
        )

        editMessage(
            ctx,
            localizedString(Strings.TITLE_CHANNEL_IS_SELECTED).format("<b><a href=\"https://www.twitch.tv/$twitchChannel\">$twitchChannel</a></b>"),
            inlineKeyboardMarkup.build()
        )
    }

    private fun editMessage(ctx: CallbackQueryContext, text: String, replyMarkup: InlineKeyboardMarkup? = null) =
        ctx.editMessage(text, replyMarkup)
            .setParseMode(ParseMode.HTML)
            .disableWebPagePreview()
            .call(ctx.sender)

    fun update(ctx: CallbackQueryContext) {
        if (userChannelsTable.getUserChannelsList(ownerId).isNotEmpty()) {
            editMessage(ctx, localizedString(Strings.WAIT_CHECK_ONLINE_MENU), initWaitMenu())
            editMessage(ctx, localizedString(Strings.TITLE_CHOOSE_CHANNEL_OR_ACTION), init())
        } else
            editMessage(ctx, localizedString(Strings.LIST_IS_EMPTY))
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
        val userChannels = userChannelsTable.getUserChannelsList(ownerId)
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
            btnShowLive = createButtonAsList(localizedString(Strings.BTN_WHO_IS_ONLINE), BUTTON_LIVE, ownerId)
            btnClips = createButtonAsList(localizedString(Strings.BTN_GET_ALL_SCREENS), BUTTON_CLIPS, ownerId)
        }

        val keyboardMarkupBuilder = InlineKeyboardMarkup.builder()

        keyboardRow.chunked(2).forEach {
            keyboardMarkupBuilder.keyboardRow(it)
        }

        return keyboardMarkupBuilder
            .keyboardRow(btnShowLive)
            .keyboardRow(btnClips)
            .keyboardRow(createButtonAsList(localizedString(Strings.BTN_CLOSE_LIST), BUTTON_CLOSE_LIST, ownerId))
            .build()
    }

    private fun createButtonAsList(text: String, btnName: String, ownerId: Long) =
        listOf(createButton(text, BtnCallbacks.CallbackData(btnName, ownerId)))

    private fun removeChannelFromUserList(channelName: String, userId: Long) =
        userChannelsTable.delete(userId, channelName)

    private fun localizedString(key: String) =
        Strings.localizedString(key, ownerId)
}