package com.helltar.twitchviewerbot.commands.simple

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.BotCommand

class StartCommand(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        replyToMessage(localizedString(Strings.START_COMMAND_INFO))
    }
}