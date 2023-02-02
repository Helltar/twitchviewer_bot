package com.helltar.twitchviewer_bot.keyboard

import com.annimon.tgbotsmodule.commands.CommandBundle
import com.annimon.tgbotsmodule.commands.CommandRegistry
import com.annimon.tgbotsmodule.commands.SimpleCallbackQueryCommand
import com.annimon.tgbotsmodule.commands.authority.For
import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.TwitchViewerBot.Companion.addRequest
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonBack
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonChannel
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonClip
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonClips
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonCloseList
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonDeleteChannel
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonLive
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonScreenshot
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.buttonShow
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.getOwnerIdFromCbData
import com.helltar.twitchviewer_bot.localizedString

class KeyboardBundle : CommandBundle<For> {

    override fun register(registry: CommandRegistry<For>) {
        registry.run {
            splitCallbackCommandByWhitespace()
            register(SimpleCallbackQueryCommand(buttonBack) { click(it, buttonBack) })
            register(SimpleCallbackQueryCommand(buttonChannel) { click(it, buttonChannel) })
            register(SimpleCallbackQueryCommand(buttonClip) { click(it, buttonClip) })
            register(SimpleCallbackQueryCommand(buttonClips) { click(it, buttonClips) })
            register(SimpleCallbackQueryCommand(buttonCloseList) { click(it, buttonCloseList) })
            register(SimpleCallbackQueryCommand(buttonDeleteChannel) { click(it, buttonDeleteChannel) })
            register(SimpleCallbackQueryCommand(buttonLive) { click(it, buttonLive) })
            register(SimpleCallbackQueryCommand(buttonScreenshot) { click(it, buttonScreenshot) })
            register(SimpleCallbackQueryCommand(buttonShow) { click(it, buttonShow) })
        }
    }

    private fun click(ctx: CallbackQueryContext, buttonName: String) {
        val ownerId = getOwnerIdFromCbData(ctx.data()) ?: return
        val user = ctx.user()

        if (user.id != ownerId) {
            ctx.answer(
                String.format(localizedString(Strings.dont_touch_is_not_your_list, user.id), user.firstName)
            ).callAsync(ctx.sender)

            return
        }

        ctx.update().message = ctx.message()
        val context = MessageContext(ctx.sender, ctx.update(), "")

        addRequest("$buttonName@$ownerId", context) {
            InlineKeyboard(ctx, ownerId).run {
                when (buttonName) {
                    buttonBack -> update(ctx)
                    buttonChannel -> btnChannel(ctx)
                    buttonClip -> btnClip()
                    buttonClips -> btnClips()
                    buttonCloseList -> btnClose(ctx)
                    buttonDeleteChannel -> btnDeleteChannel(ctx)
                    buttonLive -> btnLive()
                    buttonScreenshot -> btnScreenshot(ctx)
                    buttonShow -> btnShow()
                    else -> {}
                }
            }
        }
    }
}
