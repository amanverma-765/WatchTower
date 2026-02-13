package com.riva.watchtower.utils

import com.fleeksoft.ksoup.Ksoup

object HtmlContentExtractor {

    private val NON_CONTENT_TAGS = listOf("script", "style", "noscript", "svg", "iframe")

    fun extractVisibleText(rawHtml: String): String {
        val doc = Ksoup.parse(rawHtml)
        val body = doc.body()
        body.select(NON_CONTENT_TAGS.joinToString(",")).remove()
        return body.text()
    }

    fun contentHash(rawHtml: String): String =
        HashUtils.md5(extractVisibleText(rawHtml))
}
