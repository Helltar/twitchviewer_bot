package com.helltar.twitchviewerbot.commands.twitch

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Config.javaTempDir
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.TwitchCommand
import com.helltar.twitchviewerbot.twitch.Twitch
import com.helltar.twitchviewerbot.utils.ProcessUtils.ffmpegPrepareClip
import com.helltar.twitchviewerbot.utils.ProcessUtils.kill
import com.helltar.twitchviewerbot.utils.ProcessUtils.startStreamlinkProcess
import com.helltar.twitchviewerbot.utils.StringUtils.plusUUID
import com.helltar.twitchviewerbot.utils.StringUtils.toTwitchHtmlLink
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

class ClipCommand(ctx: MessageContext) : TwitchCommand(ctx) {

    private companion object {
        const val MAX_SIMULTANEOUS_CLIP_DOWNLOADS = 3
        const val MAX_STREAMLINK_CLIP_DURATION_SEC = 40L
        const val FFMPEG_PROCESS_TIMEOUT = MAX_STREAMLINK_CLIP_DURATION_SEC
        val log = KotlinLogging.logger {}
    }

    private val processes = ConcurrentLinkedQueue<Process>()

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
            else
                replyToMessage(localizedString(Strings.EMPTY_ONLINE_LIST))
        }
            ?: replyToMessage(localizedString(Strings.TWITCH_EXCEPTION))
    }

    suspend fun getClip(channel: String) =
        getClipsFromAll(listOf(channel))

    private suspend fun getAndSendClips(twitchBroadcastData: List<Twitch.BroadcastData>) = coroutineScope {
        twitchBroadcastData.chunked(MAX_SIMULTANEOUS_CLIP_DOWNLOADS).forEach { chunk ->
            ensureActive()

            val localizedMessage = localizedString(Strings.START_GET_CLIP)
            val chunkHtmlLinks = chunk.joinToString { it.login.toTwitchHtmlLink(it.username) }
            val tempMessage = localizedMessage.format(chunkHtmlLinks)
            val tempMessageId = replyToMessage(tempMessage)

            val jobs =
                chunk.map { broadcastData ->
                    launch {
                        processClip(broadcastData)
                    }
                }

            try {
                jobs.joinAll()
            } catch (e: CancellationException) {
                log.warn { "cancel all user-$userId jobs (${jobs.size}) and destroy processes (${processes.size}): ${e.message}" }
                processes.forEach { it.kill() }
            } finally {
                processes.clear()
                deleteMessage(tempMessageId)
            }
        }
    }

    private suspend fun processClip(broadcastData: Twitch.BroadcastData) {
        val channelLogin = broadcastData.login
        val tempName = channelLogin.plusUUID()
        val streamlinkOutFilename = "$javaTempDir/streamlink_$tempName.mp4"
        val ffmpegOutFilename = "$javaTempDir/ffmpeg_$tempName.mp4"

        try {
            ensureActive {
                startStreamlinkProcess(channelLogin, streamlinkOutFilename)
                    .wait(MAX_STREAMLINK_CLIP_DURATION_SEC)
            }

            ensureActive {
                ffmpegPrepareClip(streamlinkOutFilename, ffmpegOutFilename, MAX_STREAMLINK_CLIP_DURATION_SEC)
                    .wait(FFMPEG_PROCESS_TIMEOUT)
            }

            if (File(ffmpegOutFilename).exists())
                replyToMessageWithVideo(ffmpegOutFilename, createHtmlCaption(broadcastData))
            else
                replyToMessage(localizedString(Strings.GET_CLIP_FAIL))
        } catch (e: Exception) {
            log.error { "error processing clip for $channelLogin: ${e.message}" }
        } finally {
            File(ffmpegOutFilename).delete()
            File(streamlinkOutFilename).delete()
        }
    }

    private suspend inline fun ensureActive(block: () -> Unit) {
        block()
        coroutineContext.ensureActive()
    }

    private fun Process.wait(timeout: Long) {
        processes.add(this)

        try {
            if (!this.waitFor(timeout, TimeUnit.SECONDS))
                this.destroy()
        } finally {
            processes.remove(this)
        }
    }
}
