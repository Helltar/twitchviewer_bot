package com.helltar.twitchviewerbot.utils

import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.management.ManagementFactory
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object Utils {

    private val log = LoggerFactory.getLogger(javaClass)

    fun randomUUID() =
        UUID.randomUUID().toString()

    fun runProcess(command: String, workDir: String) {
        try {
            val file = File(workDir)

            if (!file.exists()) {
                if (!file.mkdir()) return
            } else if (!file.isDirectory) return

            ProcessBuilder(command.split(" ")).directory(file).start().waitFor()
        } catch (e: Exception) {
            log.error(e.message, e)
        }
    }

    fun getFirstRegexGroup(text: String, regex: String): String {
        val m = Pattern.compile(regex).matcher(text)
        return if (m.find()) {
            if (m.groupCount() >= 1) m.group(1) else ""
        } else ""
    }

    fun replaceTitleTag(text: String) = text
        .replace("""[^\p{L}\p{Z}\d]""".toRegex(), "")
        .replace("""\s""".toRegex(), "_")

    fun getSysStat() =
        "<code>Threads: ${ManagementFactory.getThreadMXBean().threadCount}\n${getMemUsage()}\n${getJVMUptime()}</code>"

    fun escapeHtml(text: String) = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#039;")

    private fun getJVMUptime() =
        ManagementFactory.getRuntimeMXBean().run {
            TimeUnit.MILLISECONDS.run {
                "Uptime: " +
                        "${toDays(uptime)} d. " +
                        "${toHours(uptime) % 24} h. " +
                        "${toMinutes(uptime) % 60} m. " +
                        "${toSeconds(uptime) % 60} s."
            }
        }

    private fun getMemUsage() =
        ManagementFactory.getMemoryMXBean().heapMemoryUsage.run {
            "Memory: ${used / (1024 * 1024)} / ${max / (1024 * 1024)}"
        }
}
