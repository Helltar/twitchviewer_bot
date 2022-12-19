package com.helltar.twitchviewer_bot.commands

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import com.helltar.twitchviewer_bot.localizedString
import java.io.File

abstract class BotCommand(val bot: Bot, val message: Message, val args: List<String> = listOf()) {

    protected val userId = message.from!!.id
    protected val chatId = ChatId.fromId(message.chat.id)
    protected val replyToMessageId = message.messageId

    abstract fun run()

    protected fun localizedString(key: String): String {
        return localizedString(key, userId)
    }

    protected fun sendMessage(
        text: String, replyTo: Long = replyToMessageId,
        disableWebPagePreview: Boolean = true, replyMarkup: ReplyMarkup? = null
    ) =
        bot.sendMessage(
            chatId, text, ParseMode.HTML, disableWebPagePreview,
            replyToMessageId = replyTo, allowSendingWithoutReply = true,
            replyMarkup = replyMarkup
        ).get().messageId

    protected fun sendPhoto(url: String, caption: String, replyTo: Long) =
        bot.sendPhoto(
            chatId, TelegramFile.ByUrl(url), caption, replyToMessageId = replyTo, allowSendingWithoutReply = true
        )

    protected fun sendPhoto(file: File, caption: String, replyTo: Long) =
        bot.sendPhoto(
            chatId, TelegramFile.ByFile(file), caption, replyToMessageId = replyTo, allowSendingWithoutReply = true
        )

    protected fun editMessageText(text: String, messageId: Long, replyMarkup: ReplyMarkup? = null) {
        bot.editMessageText(
            chatId, messageId,
            text = text, parseMode = ParseMode.HTML,
            disableWebPagePreview = true, replyMarkup = replyMarkup
        )
    }

    protected fun deleteMessage(messageId: Long) =
        bot.deleteMessage(chatId, messageId)
}
