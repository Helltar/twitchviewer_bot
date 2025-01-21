package com.helltar.twitchviewerbot.twitch

import com.helltar.twitchviewerbot.Config.javaTempDir
import org.slf4j.LoggerFactory
import java.io.File

object Utils {

    private val log = LoggerFactory.getLogger(javaClass)

    fun ffmpegGenerateClip(inputFilename: String, outFilename: String): Process {
        // if -fs > 10M --> black video preview (thumbnail) (telegram)
        val command = listOf("ffmpeg", "-i", inputFilename, "-fs", "9.9M", "-t", "45", "-c", "copy", "-loglevel", "quiet", outFilename)
        return executeFFmpeg(command)
    }

    fun ffmpegExtractFrame(inputFilename: String, outFilename: String): Process {
        val command = listOf("ffmpeg", "-ss", "00:00:05", "-i", inputFilename, "-vframes", "1", outFilename)
        return executeFFmpeg(command)
    }

    fun executeStreamlink(sigintTimeout: Int, channelName: String, outFilename: String): Process {
        val command =
            listOf(
                "timeout", "-k", "10", "-s", "SIGINT", "$sigintTimeout",
                "streamlink", "--twitch-disable-ads", "https://www.twitch.tv/$channelName", "720p,720p60,best", "-o", outFilename
            )

        return startProcess(command) ?: throw RuntimeException("failed to start streamlink process for channel: $channelName")
    }

    private fun executeFFmpeg(command: List<String>) =
        startProcess(command) ?: throw RuntimeException("failed to start ffmpeg process: ${command.joinToString(" ")}")

    private fun startProcess(command: List<String>): Process? =
        try {
            ProcessBuilder(command)
                .directory(File(javaTempDir))
                .start()
        } catch (e: Exception) {
            log.error(e.message, e)
            null
        }
}
