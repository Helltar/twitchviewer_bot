package com.helltar.twitchviewerbot.commands.commands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.BotConfig.DIR_TEMP
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand
import com.helltar.twitchviewerbot.utils.Utils
import com.helltar.twitchviewerbot.utils.Utils.runProcess
import java.io.File

open class ClipCompressCommand(ctx: MessageContext, args: List<String> = listOf()) : TwitchCommand(ctx, args) {

    override fun run() {
        val video = ctx.message().replyToMessage.video ?: return
        val text = ctx.message().text ?: return
        if (!text.startsWith(".")) return

        val tempMessageId = replyToMessage(localizedString(Strings.wait_clip_compress))

        // todo: ?
        try {
            val filename = DIR_TEMP + "video_${Utils.randomUUID()}.mp4"
            val file = Methods.getFile(video.fileId).call(ctx.sender)
            ctx.sender.downloadFile(file, File(filename))
            compressAndSendVideo(filename)
        } catch (e: Exception) {
            replyToMessage(localizedString(Strings.clip_compress_fail))
        }

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
            replyToMessageWithVideo(ffmpegOutFilename)
            File(ffmpegOutFilename).delete()
        } else
            replyToMessage(localizedString(Strings.clip_compress_fail))
    }
}
