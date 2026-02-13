package com.riva.watchtower.utils

import java.net.URI

object UrlUtils {
    private const val FAVICON_SIZE = 64
    private const val FAVICON_BASE_URL = "https://www.google.com/s2/favicons"

    fun extractDomain(url: String): String = try {
        val host = URI(url).host ?: url
        host.removePrefix("www.")
    } catch (_: Exception) {
        url.removePrefix("https://").removePrefix("http://")
            .removePrefix("www.").split("/").first()
    }

    fun faviconUrl(domain: String): String =
        "$FAVICON_BASE_URL?domain=$domain&sz=$FAVICON_SIZE"

    fun friendlyName(domain: String): String =
        domain.substringBeforeLast(".").replaceFirstChar { it.uppercase() }
}
