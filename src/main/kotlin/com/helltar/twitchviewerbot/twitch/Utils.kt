package com.helltar.twitchviewerbot.twitch

import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

object Utils {

    private val tempDir = System.getProperty("java.io.tmpdir")
    private val log = LoggerFactory.getLogger(javaClass)

    fun getShortClip(channel: String): String {
        val tempName = channel.plusUUID()
        val ffmpegOutFilename = "ffmpeg_$tempName.mp4"
        val streamlinkOutFilename = "streamlink_$tempName.mp4"

        executeStreamlink(35, channel, streamlinkOutFilename)

        // if -fs > 10M --> black video preview (thumbnail) (telegram)
        val command = listOf("ffmpeg", "-i", streamlinkOutFilename, "-fs", "9.9M", "-t", "45", "-c", "copy", "-loglevel", "quiet", ffmpegOutFilename)
        startProcess(command)

        File("$tempDir/$streamlinkOutFilename").delete()

        return "$tempDir/$ffmpegOutFilename"
    }

    fun getScreenshot(channel: String): String {
        val tempName = channel.plusUUID()
        val ffmpegOutFilename = "ffmpeg_$tempName.png"
        val streamlinkOutFilename = "streamlink_$tempName.mp4"

        executeStreamlink(30, channel, streamlinkOutFilename)

        val command = listOf("ffmpeg", "-ss", "00:00:05", "-i", streamlinkOutFilename, "-vframes", "1", ffmpegOutFilename)
        startProcess(command)

        File("$tempDir/$streamlinkOutFilename").delete()

        return "$tempDir/$ffmpegOutFilename"
    }

    private fun executeStreamlink(sigintTimeout: Int, channel: String, outFilename: String) {
        val command =
            listOf(
                "timeout", "-k", "10", "-s", "SIGINT", "$sigintTimeout",
                "streamlink", "--twitch-disable-ads", "https://www.twitch.tv/$channel", "720p,720p60,best", "-o", outFilename
            )

        startProcess(command)
    }

    private fun startProcess(command: List<String>) = try {
        ProcessBuilder(command)
            .directory(File(tempDir))
            .start()
            .waitFor(60, TimeUnit.SECONDS)
    } catch (e: Exception) {
        log.error(e.message, e)
    }

    private fun String.plusUUID() =
        "${this}_${UUID.randomUUID()}"
}