package com.helltar.twitchviewerbot.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Strings
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import java.io.File
import java.net.URI
import java.util.concurrent.CompletableFuture

abstract class BotCommand(val ctx: MessageContext) {

    val argumentsString: String = ctx.argumentsAsString()

    protected val userId = ctx.user().id
    protected val arguments: Array<String> = ctx.arguments()

    abstract suspend fun run()

    fun replyToMessage(
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

    protected fun replyToMessageWithMediaGroup(media: List<InputMediaPhoto>) {
        ctx.replyWithMediaGroup()
            .setMedias(media)
            .setReplyToMessageId(ctx.messageId())
            .call(ctx.sender)
    }

    protected fun localizedString(key: String) =
        Strings.localizedString(key, ctx.message().from.languageCode)

    protected fun replyToMessageWithPhoto(url: String, caption: String): Message =
        ctx.replyToMessageWithPhoto()
            .setFile(url, URI.create(url).toURL().openStream())
            .setCaption(caption)
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

    protected fun deleteMessage(messageId: Int): CompletableFuture<Boolean> =
        ctx.deleteMessage().setMessageId(messageId).callAsync(ctx.sender)
}
