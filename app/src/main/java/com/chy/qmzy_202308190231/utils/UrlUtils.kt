package com.chy.qmzy_202308190231.utils

object UrlUtils {
    fun isUrl(text: String): Boolean {
        val t = text.trim()
        return t.startsWith("http://", ignoreCase = true) ||
            t.startsWith("https://", ignoreCase = true) ||
            t.startsWith("www.", ignoreCase = true)
    }

    fun normalizeUrl(url: String): String {
        val u = url.trim()
        if (u.startsWith("http://", ignoreCase = true) || u.startsWith("https://", ignoreCase = true)) {
            return u
        }
        return "https://$u"
    }
}