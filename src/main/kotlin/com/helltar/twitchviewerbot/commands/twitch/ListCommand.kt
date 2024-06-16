package com.helltar.twitchviewerbot.commands.twitch

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks
import com.helltar.twitchviewerbot.commands.twitch.keyboard.ButtonCallbacks.string
import com.helltar.twitchviewerbot.commands.twitch.keyboard.InlineKeyboard
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class ListCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    override suspend fun run() {
        if (isUserListEmpty()) {
            replyToMessage(localizedString(Strings.LIST_IS_EMPTY))
            return
        }

        val update =
            ctx.update().apply {
                callbackQuery =
                    CallbackQuery().apply {
                        from = ctx.user()
                        message = ctx.message()
                        data = ButtonCallbacks.CallbackData().string()
                    }
            }

        val callbackQueryContext = CallbackQueryContext(ctx.sender, update, String())
        val inlineKeyboard = InlineKeyboard(callbackQueryContext, userId)

        val messageId = replyToMessage(localizedString(Strings.WAIT_CHECK_ONLINE_MENU), replyMarkup = inlineKeyboard.waitingMenu())
        editMessageText(localizedString(Strings.TITLE_CHOOSE_CHANNEL_OR_ACTION), messageId, inlineKeyboard.mainMenu())
    }
}