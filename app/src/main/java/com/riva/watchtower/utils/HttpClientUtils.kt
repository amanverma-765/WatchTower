package com.riva.watchtower.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

data class HttpClientConfig(
    val baseUrl: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val userAgent: String = "Mozilla/5.0 (Linux; Android 13; SM-G981B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Mobile Safari/537.36",
    val requestTimeoutMillis: Long = 30_000,
    val connectTimeoutMillis: Long = 30_000,
    val socketTimeoutMillis: Long = 30_000,
    val enableLogging: Boolean = true,
    val logLevel: LogLevel = LogLevel.ALL
)

internal object HttpClientFactory {
    internal fun create(config: HttpClientConfig = HttpClientConfig()): HttpClient {
        return HttpClient(OkHttp) {
            engine {
                config {
                    retryOnConnectionFailure(true)
                    followRedirects(true)
                    connectTimeout(30, TimeUnit.SECONDS)
                    readTimeout(30, TimeUnit.SECONDS)
                    writeTimeout(30, TimeUnit.SECONDS)
                }
            }

            install(ContentNegotiation) {
                json(
                    json = Json {
                        explicitNulls = false
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                    }
                )
            }

            install(HttpTimeout) {
                requestTimeoutMillis = config.requestTimeoutMillis
                connectTimeoutMillis = config.connectTimeoutMillis
                socketTimeoutMillis = config.socketTimeoutMillis
            }

            install(HttpCookies) {
                storage = AcceptAllCookiesStorage()
            }

            if (config.enableLogging) {
                install(Logging) {
                    logger = object : io.ktor.client.plugins.logging.Logger {
                        override fun log(message: String) {
                            println("Ktor -> $message")
                        }
                    }
                    level = config.logLevel
                }
            }

            install(DefaultRequest) {
                // Set base URL if provided
                config.baseUrl?.let { baseUrl ->
                    url(baseUrl)
                }

                // Set User-Agent header
                header("User-Agent", config.userAgent)

                // Set additional headers
                config.headers.forEach { (key, value) ->
                    header(key, value)
                }
            }
        }
    }
}