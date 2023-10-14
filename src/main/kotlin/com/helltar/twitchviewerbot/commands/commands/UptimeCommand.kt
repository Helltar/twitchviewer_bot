package com.helltar.twitchviewerbot.commands.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.commands.BotCommand
import com.helltar.twitchviewerbot.utils.Utils.getSysStat

class UptimeCommand(ctx: MessageContext) : BotCommand(ctx) {

    override fun run() {
        replyToMessage(getSysStat())
    }
}