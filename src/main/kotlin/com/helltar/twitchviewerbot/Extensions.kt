package com.helltar.twitchviewerbot

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
}