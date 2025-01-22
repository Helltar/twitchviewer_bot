package com.helltar.twitchviewerbot.twitch

import com.helltar.twitchviewerbot.Config.javaTempDir
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

object Utils {

    private val log = KotlinLogging.logger {}

    fun ffmpegGenerateClip(inputFilename: String, outFilename: String): Process {
        // if -fs > 10M --> black video preview (thumbnail) (telegram)
        val command = listOf("ffmpeg", "-i", inputFilename, "-fs", "9.9M", "-t", "45", "-c", "copy", "-loglevel", "quiet", outFilename)
        return startProcess(command) ?: throw RuntimeException("failed to start ffmpeg process: ${command.joinToString(" ")}")
    }

    fun executeStreamlink(sigintTimeout: Int, channelName: String, outFilename: String): Process {
        val command =
            listOf(
                "timeout", "-k", "10", "-s", "SIGINT", "$sigintTimeout",
                "streamlink", "--twitch-disable-ads", "https://www.twitch.tv/$channelName", "720p,720p60,best", "-o", outFilename
            )

        return startProcess(command) ?: throw RuntimeException("failed to start streamlink process for channel: $channelName")
    }

    private fun startProcess(command: List<String>): Process? =
        try {
            ProcessBuilder(command)
                .directory(File(javaTempDir))
                .start()
        } catch (e: Exception) {
            log.error(e) { e.message }
            null
        }
}
