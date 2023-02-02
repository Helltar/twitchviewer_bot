package com.helltar.twitchviewer_bot.commands.commands

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.commands.TwitchCommand
import com.helltar.twitchviewer_bot.keyboard.InlineKeyboard
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class ListCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    override fun run() {
        if (isBot) {
            super.run()
            return
        }

        if (!isUserListNotEmpty()) {
            replyToMessage(localizedString(Strings.list_is_empty))
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
            localizedString(Strings.title_choose_channel_or_action),
            replyToMessage(
                localizedString(Strings.wait_check_online_menu),
                inlineKeyboard.initWaitMenu()
            ),
            inlineKeyboard.init()
        )
    }
}
