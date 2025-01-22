package com.helltar.twitchviewerbot.commands.twitch

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Config.javaTempDir
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand
import com.helltar.twitchviewerbot.twitch.Twitch
import com.helltar.twitchviewerbot.twitch.Utils.createTwitchHtmlLink
import com.helltar.twitchviewerbot.twitch.Utils.ffmpegGenerateClip
import com.helltar.twitchviewerbot.twitch.Utils.plusUUID
import com.helltar.twitchviewerbot.twitch.Utils.startStreamlinkProcess
import com.helltar.twitchviewerbot.twitch.Utils.toHashTag
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

class ClipCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    private companion object {
        const val MAX_SIMULTANEOUS_CLIP_DOWNLOADS = 3
        val log = KotlinLogging.logger {}
    }

    override suspend fun run() {
        if (arguments.isEmpty()) {
            if (isUserListNotEmpty())
                getClipsFromAll(getUserChannelsList())
            else
                replyToMessage(localizedString(Strings.CLIP_COMMAND_INFO))
        } else {
            val channel = arguments.first()

            if (checkChannelNameAndReplyIfInvalid(channel))
                getClip(channel)
        }
    }

    suspend fun getClipsFromAll(userLogins: List<String>) {
        twitch.getOnlineList(userLogins)?.let {
            if (it.isNotEmpty())
                getAndSendClips(it)
            else {
                val text = if (userLogins.size > 1)
                    localizedString(Strings.EMPTY_ONLINE_LIST)
                else
                    localizedString(Strings.STREAM_OFFLINE).format(userLogins.first())

                replyToMessage(text)
            }
        }
            ?: replyToMessage(localizedString(Strings.TWITCH_EXCEPTION))
    }

    suspend fun getClip(channel: String) =
        getClipsFromAll(listOf(channel))

    private suspend fun getAndSendClips(twitchBroadcastData: List<Twitch.BroadcastData>) = coroutineScope {
        twitchBroadcastData.chunked(MAX_SIMULTANEOUS_CLIP_DOWNLOADS).forEach { chunk ->
            ensureActive()

            val tempMessage = localizedString(Strings.START_GET_CLIP).format(chunk.joinToString { createTwitchHtmlLink(it.login, it.username) })
            val tempMessageId = replyToMessage(tempMessage)

            val processes = ConcurrentLinkedQueue<Process>()

            val jobs =
                chunk.map { broadcastData ->
                    launch {
                        val channelLogin = broadcastData.login
                        val tempName = channelLogin.plusUUID()
                        val ffmpegOutFilename = "ffmpeg_$tempName.mp4"
                        val streamlinkOutFilename = "streamlink_$tempName.mp4"
                        val clipFilename = "$javaTempDir/$ffmpegOutFilename"

                        try {
                            val streamlinkProcess = startStreamlinkProcess(35, channelLogin, streamlinkOutFilename).also { processes.add(it) }

                            if (!streamlinkProcess.waitFor(60, TimeUnit.SECONDS))
                                streamlinkProcess.destroy()

                            ensureActive()

                            val ffmpegProcess = ffmpegGenerateClip(streamlinkOutFilename, ffmpegOutFilename).also { processes.add(it) }

                            if (!ffmpegProcess.waitFor(40, TimeUnit.SECONDS))
                                ffmpegProcess.destroy()

                            ensureActive()

                            if (!File(clipFilename).exists()) {
                                replyToMessage(localizedString(Strings.GET_CLIP_FAIL))
                                return@launch
                            }

                            val channelUsername = broadcastData.username
                            val streamCategory = broadcastData.gameName

                            val titleHtml = "${createTwitchHtmlLink(channelLogin, channelUsername)} - ${broadcastData.title}\n\n"
                            val categoryHtml = if (streamCategory.isNotEmpty()) ", #${streamCategory.toHashTag()}" else ""
                            val startTimeHtml = localizedString(Strings.STREAM_START_TIME).format(broadcastData.uptime) + "\n\n"
                            val viewersHtml = localizedString(Strings.STREAM_VIEWERS).format(broadcastData.viewerCount) + "\n"

                            replyToMessageWithVideo(clipFilename, "$titleHtml$viewersHtml$startTimeHtml#${channelUsername}$categoryHtml")
                        } catch (e: Exception) {
                            log.error { "error processing clip for $channelLogin: ${e.message}" }
                        } finally {
                            File("$javaTempDir/$streamlinkOutFilename").delete()
                            File(clipFilename).delete()
                        }
                    }
                }

            try {
                jobs.joinAll()
            } catch (e: CancellationException) {
                log.warn { "cancel all user-$userId jobs (${jobs.size}) and destroy processes (${processes.size}), message: ${e.message}" }

                processes.forEach {
                    if (it.isAlive) {
                        log.warn { "destroying process ${it.pid()} due to user-$userId-task cancellation" }
                        it.destroy()
                        it.waitFor(5, TimeUnit.SECONDS)
                        if (it.isAlive) it.destroyForcibly()
                    }
                }
            } finally {
                deleteMessage(tempMessageId)
            }
        }
    }
}
