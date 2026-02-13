package com.riva.watchtower.utils

import java.net.URI

object UrlUtils {
    private const val FAVICON_SIZE = 64
    private const val GOOGLE_FAVICON_URL = "https://www.google.com/s2/favicons"
    private const val DUCKDUCKGO_FAVICON_URL = "https://icons.duckduckgo.com/ip3"

    fun extractDomain(url: String): String = try {
        val host = URI(url).host ?: url
        host.removePrefix("www.")
    } catch (_: Exception) {
        url.removePrefix("https://").removePrefix("http://")
            .removePrefix("www.").split("/").first()
    }

    fun faviconUrl(domain: String): String =
        "$GOOGLE_FAVICON_URL?domain=$domain&sz=$FAVICON_SIZE"

    fun faviconUrlFallback(domain: String): String =
        "$DUCKDUCKGO_FAVICON_URL/$domain.ico"

    fun friendlyName(domain: String): String =
        domain.substringBeforeLast(".").replaceFirstChar { it.uppercase() }
}
