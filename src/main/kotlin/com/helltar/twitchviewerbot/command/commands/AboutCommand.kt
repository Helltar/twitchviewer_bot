package com.helltar.twitchviewerbot.command.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.command.BotCommand

class AboutCommand(ctx: MessageContext) : BotCommand(ctx) {

    override fun run() {
        replyToMessage(
            """
            <a href="https://github.com/Helltar/twitchviewer_bot">Twitch Viewer Bot</a>
            Contact: @Helltar https://helltar.com
            Source Code:
        """.trimIndent(), true
        )
    }
}