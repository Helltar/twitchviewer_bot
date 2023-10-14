package com.helltar.twitchviewerbot.commands.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.BotCommand

class StartCommand(ctx: MessageContext) : BotCommand(ctx) {

    override fun run() {
        replyToMessage(localizedString(Strings.start_command_info))
    }
}