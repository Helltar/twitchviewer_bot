package com.helltar.twitchviewerbot.command.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.command.BotCommand

class HelpCommand(ctx: MessageContext) : BotCommand(ctx) {

    override fun run() {
        replyToMessage(localizedString(Strings.START_COMMAND_INFO))
    }
}