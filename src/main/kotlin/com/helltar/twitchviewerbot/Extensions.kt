package com.helltar.twitchviewerbot

import java.util.*

object Extensions {

    fun String.toHashTag() =
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

    fun String.randomizeByName() =
        "${this}_${UUID.randomUUID()}"
}