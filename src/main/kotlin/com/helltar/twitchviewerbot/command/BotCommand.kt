package com.helltar.twitchviewerbot.command

import com.annimon.tgbotsmodule.api.methods.Methods
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import java.io.File
import java.io.Serializable
import java.net.URI
import java.util.concurrent.CompletableFuture

abstract class BotCommand(val ctx: MessageContext) {

    val argumentsAsString: String = ctx.argumentsAsString()

    protected val userId = ctx.user().id
    protected val arguments: Array<String> = ctx.arguments()

    abstract fun run()

    protected fun localizedString(key: String) =
        Strings.localizedString(key, userId)

    protected fun replyToMessage(
        text: String,
        enableWebPagePreview: Boolean = false,
        replyMarkup: InlineKeyboardMarkup? = null
    ): Int =
        ctx.replyToMessage(text)
            .setReplyMarkup(replyMarkup)
            .setParseMode(ParseMode.HTML)
            .setWebPagePreviewEnabled(enableWebPagePreview)
            .call(ctx.sender)
            .messageId

    protected fun replyToMessageWithPhoto(file: File, caption: String): Message =
        ctx.replyToMessageWithPhoto()
            .setFile(file)
            .setCaption(caption)
            .setParseMode(ParseMode.HTML)
            .call(ctx.sender)

    protected fun replyToMessageWithPhoto(url: String, caption: String, messageId: Int = ctx.messageId()): Message =
        ctx.replyToMessageWithPhoto()
            .setFile(url, URI.create(url).toURL().openStream())
            .setCaption(caption)
            .setReplyToMessageId(messageId)
            .setParseMode(ParseMode.HTML)
            .call(ctx.sender)

    protected fun replyToMessageWithVideo(
        filename: String,
        caption: String = "",
        duration: Int = 15,
        height: Int = 1080,
        width: Int = 1920
    ): Message =
        ctx.replyToMessageWithVideo()
            .setFile(File(filename))
            .setCaption(caption)
            .setDuration(duration)
            .setHeight(height)
            .setWidth(width)
            .setParseMode(ParseMode.HTML)
            .call(ctx.sender)

    protected fun editMessageText(text: String, messageId: Int, replyMarkup: InlineKeyboardMarkup): Serializable =
        Methods.editMessageText(ctx.chatId(), messageId, text)
            .setReplyMarkup(replyMarkup)
            .setParseMode(ParseMode.HTML)
            .disableWebPagePreview()
            .call(ctx.sender)

    protected fun deleteMessage(messageId: Int): CompletableFuture<Boolean> =
        ctx.deleteMessage().setMessageId(messageId).callAsync(ctx.sender)
}