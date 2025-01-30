package com.helltar.twitchviewerbot.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.TimeUnit

object ProcessUtils {

    private val log = KotlinLogging.logger {}

    fun ffmpegPrepareClip(inputFilename: String, outFilename: String, lengthTime: Long): Process {
        val process =
            listOf(
                "ffmpeg", "-i", inputFilename,
                "-fs", "9.9M", // if the file size exceeds 10MB, a black video thumbnail (preview) may appear on telegram
                "-t", "$lengthTime",
                "-c", "copy",
                "-loglevel", "quiet", outFilename
            )
                .startProcess()

        return process ?: throw RuntimeException("failed to start ffmpeg process: $inputFilename")
    }

    fun startStreamlinkProcess(channelName: String, outFilename: String): Process {
        val process =
            listOf(
                "streamlink", "--twitch-disable-ads",
                "https://www.twitch.tv/$channelName",
                "720p,720p60,best",
                "-o", outFilename
            )
                .startProcess()

        return process ?: throw RuntimeException("failed to start streamlink process for channel: $channelName")
    }

    fun Process.kill() {
        if (this.isAlive) {
            log.warn { "destroying process ${this.pid()}" }
            this.destroy()

            if (!this.waitFor(5, TimeUnit.SECONDS)) {
                log.warn { "force destroying process ${this.pid()} after timeout" }
                this.destroyForcibly()
            }
        }
    }

    private fun List<String>.startProcess(): Process? =
        try {
            ProcessBuilder(this).start()
        } catch (e: Exception) {
            log.error(e) { e.message }
            null
        }
}
