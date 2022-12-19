package com.helltar.twitchviewer_bot.commands.commands

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.commands.TwitchCommand
import com.helltar.twitchviewer_bot.keyboard.InlineKeyboard

class ListCommand(bot: Bot, message: Message) : TwitchCommand(bot, message) {

    override fun run() {
        if (isBot) {
            super.run()
            return
        }

        if (!isUserListNotEmpty()) {
            sendMessage(localizedString(Strings.list_is_empty))
            return
        }

        val inlineKeyboard = InlineKeyboard(bot, message, userId)

        editMessageText(
            localizedString(Strings.title_choose_channel_or_action),
            sendMessage(
                localizedString(Strings.wait_check_online_menu),
                replyMarkup = inlineKeyboard.initWaitMenu()
            ),
            inlineKeyboard.init()
        )
    }
}
