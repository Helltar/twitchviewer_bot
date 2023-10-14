package com.helltar.twitchviewerbot.commands.commands

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.BotConfig
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.twitch.Twitch
import com.helltar.twitchviewerbot.utils.Utils
import kotlinx.coroutines.*
import java.io.File

class ClipCommand(ctx: MessageContext, args: List<String> = listOf()) : ClipCompressCommand(ctx, args) {

    private companion object {
        val requestsList = hashMapOf<String, Job>()
    }

    override fun run() {
        if (args.isEmpty()) {
            if (isUserListNotEmpty())
                getClipsFromAll(getUserChannelsList())
            else
                replyToMessage(localizedString(Strings.clip_command_info))
        } else {
//            if (ctx.argumentsAsString().startsWith("youtu.be"))
//                sendYoutubeClip(ctx.argument(0))
//            else
                if (checkIsChannelNameValid()) {
                    val compress = args.size > 1 && args[1].startsWith(".")
                    getClipsFromAll(listOf(args[0]), compress)
                }
        }
    }

    fun getClipsFromAll(userLogins: List<String>, compress: Boolean = false) {
        twitch.getOnlineList(userLogins)?.let {
            if (it.isNotEmpty())
                sendClips(it, compress)
            else {
                val text = if (userLogins.size > 1)
                    localizedString(Strings.empty_online_list)
                else
                    String.format(localizedString(Strings.stream_offline), userLogins[0])

                replyToMessage(text)
            }
        }
            ?: replyToMessage(localizedString(Strings.twitch_exception))
    }

    private fun sendClips(twitchBroadcastData: List<Twitch.BroadcastData>, compress: Boolean) {
        twitchBroadcastData.forEach { broadcastData ->
            val requestKey = "$userId@${broadcastData.login}"

            addRequest(requestKey) {
                val tempMessage = String.format(localizedString(Strings.start_get_clip), broadcastData.username)
                val tempMessageId = replyToMessage(tempMessage)
                val clipFilename = twitch.getShortClip(broadcastData.login)

                if (!File(clipFilename).exists()) {
                    replyToMessage(localizedString(Strings.get_clip_fail))
                    deleteMessage(tempMessageId)
                    return@addRequest
                }

                if (!compress) {
                    broadcastData.run {
                        val htmlTitle = "<b><a href=\"https://www.twitch.tv/$login\">$username</a></b> - $title\n\n"
                        val viewers = "\uD83D\uDC40 <b>$viewerCount</b>\n" // 👀
                        val time = String.format(localizedString(Strings.stream_start_time), startedAt, uptime) + "\n\n"
                        val gameName = if (gameName.isNotEmpty()) ", #${Utils.replaceTitleTag(gameName)}" else ""

                        replyToMessageWithVideo(clipFilename, "$htmlTitle$viewers$time#${username}$gameName")
                    }

                    File(clipFilename).delete()
                } else
                    compressAndSendVideo(clipFilename)

                deleteMessage(tempMessageId)
            }
        }
    }

    private fun addRequest(requestKey: String, func: () -> Unit) {
        if (requestsList.containsKey(requestKey)) {
            if (requestsList[requestKey]?.isCompleted == false) {
                replyToMessage(localizedString(Strings.many_request))
                return
            }
        } // todo: remove completed

        requestsList[requestKey] = CoroutineScope(Dispatchers.IO)
            .launch(CoroutineName(requestKey)) {
                func()
            }
    }

    private fun sendYoutubeClip(url: String) {
        val requestKey = "$userId@$url"

        addRequest(requestKey) {
            val tempMessage = String.format(localizedString(Strings.start_get_clip), url)
            val tempMessageId = replyToMessage(tempMessage)
            val clipFilename = getYoutubeClip(url)

            if (!File(clipFilename).exists()) {
                replyToMessage(localizedString(Strings.get_clip_fail))
                deleteMessage(tempMessageId)
                return@addRequest
            }

            replyToMessageWithVideo(clipFilename, url, 0, 360, 640)

            File(clipFilename).delete()

            deleteMessage(tempMessageId)
        }
    }

    private fun getYoutubeClip(url: String): String {
        val tempName = Utils.randomUUID()
        val ytdlpOutFilename = "ytdlp_$tempName.mp4"

        runYtDlp(10, url, ytdlpOutFilename)

        return BotConfig.DIR_TEMP + ytdlpOutFilename
    }

    private fun runYtDlp(timeout: Int, url: String, outFilename: String) =
        Utils.runProcess(
            "timeout -k 10 -s SIGINT $timeout yt-dlp -f bv -S res:360 $url -o $outFilename",
            BotConfig.DIR_TEMP
        )
}