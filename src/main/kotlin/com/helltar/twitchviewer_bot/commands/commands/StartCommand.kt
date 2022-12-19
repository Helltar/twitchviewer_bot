package com.helltar.twitchviewer_bot.commands.commands

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.commands.BotCommand

class StartCommand(bot: Bot, message: Message) : BotCommand(bot, message) {

    override fun run() {
        sendMessage(localizedString(Strings.start_hello))
    }
}
