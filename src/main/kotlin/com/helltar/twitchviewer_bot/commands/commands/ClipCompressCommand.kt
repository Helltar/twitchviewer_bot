package com.helltar.twitchviewer_bot.commands.commands

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.TelegramFile
import com.helltar.twitchviewer_bot.BotConfig.DIR_TEMP
import com.helltar.twitchviewer_bot.Strings
import com.helltar.twitchviewer_bot.commands.TwitchCommand
import com.helltar.twitchviewer_bot.utils.Utils
import com.helltar.twitchviewer_bot.utils.Utils.runProcess
import java.io.File

open class ClipCompressCommand(bot: Bot, message: Message, args: List<String> = listOf()) : TwitchCommand(bot, message, args) {

    override fun run() {
        val video = message.replyToMessage?.video ?: return
        val text = message.text ?: return
        if (!text.startsWith(".")) return

        val tempMessageId = sendMessage(localizedString(Strings.wait_clip_compress))

        bot.downloadFileBytes(video.fileId)?.let {
            val filename = DIR_TEMP + "video_${Utils.randomUUID()}.mp4"
            File(filename).writeBytes(it)
            compressAndSendVideo(filename)
        }
            ?: sendMessage(localizedString(Strings.clip_compress_fail))

        deleteMessage(tempMessageId)
    }

    protected fun compressAndSendVideo(filename: String) {
        val ffmpegOutFilename = DIR_TEMP + "video_${Utils.randomUUID()}.mp4"

        runProcess(
            "timeout -k 5 -s SIGINT 60 " +
                    "ffmpeg -i ${File(filename).name} -b:v 500k -preset ultrafast ${File(ffmpegOutFilename).name}",
            DIR_TEMP
        )

        File(filename).delete()

        if (File(ffmpegOutFilename).exists()) {
            bot.sendVideo(
                chatId,
                TelegramFile.ByFile(File(ffmpegOutFilename)),
                replyToMessageId = replyToMessageId, allowSendingWithoutReply = true
            )

            File(ffmpegOutFilename).delete()
        } else
            sendMessage(localizedString(Strings.clip_compress_fail))
    }
}
