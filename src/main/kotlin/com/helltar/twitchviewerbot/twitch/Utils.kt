package com.helltar.twitchviewerbot.twitch

import com.helltar.twitchviewerbot.Config.javaTempDir
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.util.*

object Utils {

    private val log = KotlinLogging.logger {}

    fun ffmpegGenerateClip(inputFilename: String, outFilename: String): Process {
        // if the file size (-fs) exceeds 10MB, a black video thumbnail (preview) may appear on telegram
        val command = listOf("ffmpeg", "-i", inputFilename, "-fs", "9.9M", "-t", "45", "-c", "copy", "-loglevel", "quiet", outFilename)
        return startProcess(command) ?: throw RuntimeException("failed to start ffmpeg process: ${command.joinToString(" ")}")
    }

    fun startStreamlinkProcess(sigintTimeout: Int, channelName: String, outFilename: String): Process {
        val command =
            listOf(
                "timeout", "-k", "10", "-s", "SIGINT", "$sigintTimeout",
                "streamlink", "--twitch-disable-ads", "https://www.twitch.tv/$channelName", "720p,720p60,best", "-o", outFilename
            )

        return startProcess(command) ?: throw RuntimeException("failed to start streamlink process for channel: $channelName")
    }

    fun createTwitchHtmlLink(login: String, username: String) =
        """<b><a href="https://www.twitch.tv/$login">$username</a></b>"""

    fun String.escapeHtml() =
        this
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#039;")

    fun String.toHashTag() =
        this.replace("""[^\p{L}\p{Z}\d]""".toRegex(), "").replace("""\s""".toRegex(), "_")

    fun String.plusUUID() =
        "${this}_${UUID.randomUUID()}"

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
