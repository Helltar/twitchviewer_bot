package com.helltar.twitchviewer_bot.commands.commands

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import com.helltar.twitchviewer_bot.commands.BotCommand
import com.helltar.twitchviewer_bot.utils.Utils.getSysStat

class UptimeCommand(bot: Bot, message: Message) : BotCommand(bot, message) {

    override fun run() {
        sendMessage(getSysStat())
    }
}
