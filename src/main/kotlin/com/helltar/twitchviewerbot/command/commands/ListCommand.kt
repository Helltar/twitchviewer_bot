package com.helltar.twitchviewerbot.command.commands

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.command.TwitchCommand
import com.helltar.twitchviewerbot.keyboard.InlineKeyboard
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class ListCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    override fun run() {
        if (isUserListEmpty()) {
            replyToMessage(localizedString(Strings.LIST_IS_EMPTY))
            return
        }

        // todo: ?
        ctx.update().callbackQuery =
            CallbackQuery().apply {
                from = ctx.user()
                message = ctx.message()
                data = "0 $userId 0 0"
            }

        val callbackQueryContext = CallbackQueryContext(ctx.sender, ctx.update(), "")
        val inlineKeyboard = InlineKeyboard(callbackQueryContext, userId)

        editMessageText(
            localizedString(Strings.TITLE_CHOOSE_CHANNEL_OR_ACTION),
            replyToMessage(
                localizedString(Strings.WAIT_CHECK_ONLINE_MENU),
                replyMarkup = inlineKeyboard.initWaitMenu()
            ),
            inlineKeyboard.init()
        )
    }
}