package com.helltar.twitchviewer_bot.commands.commands

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import com.helltar.twitchviewer_bot.commands.BotCommand

class AboutCommand(bot: Bot, message: Message) : BotCommand(bot, message) {

    override fun run() {
        sendMessage("""
            <a href="https://github.com/Helltar/twitchviewer_bot">Twitch Viewer Bot</a>
            Contact: @Helltar https://helltar.com
            Source Code:
        """.trimIndent(), disableWebPagePreview = false)
    }
}
