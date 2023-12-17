package com.helltar.twitchviewerbot.utils

import java.lang.management.ManagementFactory
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object Utils {

    fun getFirstRegexGroup(text: String, regex: String): String {
        val m = Pattern.compile(regex).matcher(text)

        return if (m.find()) {
            if (m.groupCount() >= 1) m.group(1) else ""
        } else ""
    }

    fun String.replaceTitleTag() =
        this
            .replace("""[^\p{L}\p{Z}\d]""".toRegex(), "")
            .replace("""\s""".toRegex(), "_")

    fun String.escapeHtml() =
        this
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#039;")

    fun getSysHtmlStat() =
        "<code>Threads: ${ManagementFactory.getThreadMXBean().threadCount}\n${getMemUsage()}\n${getJVMUptime()}</code>"

    fun getTimeZoneOffset(): Int {
        val systemTimeZone = TimeZone.getDefault()
        return systemTimeZone.rawOffset / (1000 * 60 * 60)
    }

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