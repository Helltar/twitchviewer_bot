package com.helltar.twitchviewer_bot.keyboard

import com.annimon.tgbotsmodule.commands.CommandBundle
import com.annimon.tgbotsmodule.commands.CommandRegistry
import com.annimon.tgbotsmodule.commands.SimpleCallbackQueryCommand
import com.annimon.tgbotsmodule.commands.authority.For
import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.TwitchViewerBot.Companion.addRequest
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.BUTTON_BACK
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.BUTTON_CHANNEL
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.BUTTON_CLIP
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.BUTTON_CLIPS
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.BUTTON_CLOSE_LIST
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.BUTTON_DELETE_CHANNEL
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.BUTTON_LIVE
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.BUTTON_SCREENSHOT
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.BUTTON_SHOW
import com.helltar.twitchviewer_bot.keyboard.BtnCallbacks.getOwnerIdFromCbData
import com.helltar.twitchviewer_bot.localizedString

class KeyboardBundle : CommandBundle<For> {

    override fun register(registry: CommandRegistry<For>) {
        registry.run {
            splitCallbackCommandByWhitespace()
            register(SimpleCallbackQueryCommand(BUTTON_BACK) { click(it, BUTTON_BACK) })
            register(SimpleCallbackQueryCommand(BUTTON_CHANNEL) { click(it, BUTTON_CHANNEL) })
            register(SimpleCallbackQueryCommand(BUTTON_CLIP) { click(it, BUTTON_CLIP) })
            register(SimpleCallbackQueryCommand(BUTTON_CLIPS) { click(it, BUTTON_CLIPS) })
            register(SimpleCallbackQueryCommand(BUTTON_CLOSE_LIST) { click(it, BUTTON_CLOSE_LIST) })
            register(SimpleCallbackQueryCommand(BUTTON_DELETE_CHANNEL) { click(it, BUTTON_DELETE_CHANNEL) })
            register(SimpleCallbackQueryCommand(BUTTON_LIVE) { click(it, BUTTON_LIVE) })
            register(SimpleCallbackQueryCommand(BUTTON_SCREENSHOT) { click(it, BUTTON_SCREENSHOT) })
            register(SimpleCallbackQueryCommand(BUTTON_SHOW) { click(it, BUTTON_SHOW) })
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
                    BUTTON_BACK -> update(ctx)
                    BUTTON_CHANNEL -> btnChannel(ctx)
                    BUTTON_CLIP -> btnClip()
                    BUTTON_CLIPS -> btnClips()
                    BUTTON_CLOSE_LIST -> btnClose(ctx)
                    BUTTON_DELETE_CHANNEL -> btnDeleteChannel(ctx)
                    BUTTON_LIVE -> btnLive()
                    BUTTON_SCREENSHOT -> btnScreenshot(ctx)
                    BUTTON_SHOW -> btnShow()
                    else -> {}
                }
            }
        }
    }
}
