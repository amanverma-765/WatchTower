package com.riva.watchtower.data.external

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class SiteTrackingProvider(private val httpClient: HttpClient) {
    companion object {
        val logger = Logger.withTag("SiteTrackingProvider")
    }

    suspend fun fetchSiteHtml(url: String): Result<String> {
        val response = httpClient.get(urlString = url)
        if (!response.status.isSuccess()) {
            logger.e { "Failed to fetch: $url" }
        }
        return Result.success(response.bodyAsText())
    }
}