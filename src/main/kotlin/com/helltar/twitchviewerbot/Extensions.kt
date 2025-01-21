package com.helltar.twitchviewerbot

import java.util.*

object Extensions {

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
}
