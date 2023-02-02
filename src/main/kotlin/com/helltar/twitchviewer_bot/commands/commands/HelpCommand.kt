package com.helltar.twitchviewer_bot.commands.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.commands.BotCommand

class HelpCommand(ctx: MessageContext) : BotCommand(ctx) {

    override fun run() {
        replyToMessage(localizedString(Strings.start_command_info))
    }
}
