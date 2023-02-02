package com.helltar.twitchviewer_bot.commands.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewer_bot.commands.BotCommand
import com.helltar.twitchviewer_bot.utils.Utils.getSysStat

class UptimeCommand(ctx: MessageContext) : BotCommand(ctx) {

    override fun run() {
        replyToMessage(getSysStat())
    }
}
