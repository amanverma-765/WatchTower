package com.riva.watchtower.data.local

import android.content.Context
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class HtmlStorageProvider(private val context: Context) {

    companion object {
        private val logger = Logger.withTag("HtmlStorageProvider")
    }

    private val htmlDir: File
        get() = File(context.filesDir, "site_html").also { it.mkdirs() }

    private fun baselineFile(siteId: String) = File(htmlDir, "${siteId}_baseline.html")
    private fun latestFile(siteId: String) = File(htmlDir, "${siteId}_latest.html")

    /** Save the initial baseline HTML when a site is first added. */
    suspend fun saveBaseline(siteId: String, html: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            baselineFile(siteId).writeText(html)
            logger.d { "Saved baseline for site $siteId (${html.length} chars)" }
        }.onFailure { logger.e(it) { "Failed to save baseline for site $siteId" } }
    }

    /** Save the latest fetched HTML (not yet accepted as baseline). */
    suspend fun saveLatest(siteId: String, html: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            latestFile(siteId).writeText(html)
            logger.d { "Saved latest for site $siteId (${html.length} chars)" }
        }.onFailure { logger.e(it) { "Failed to save latest for site $siteId" } }
    }

    /** Promote latest to baseline (user resolved the change). */
    suspend fun promoteLatestToBaseline(siteId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val latest = latestFile(siteId)
            val baseline = baselineFile(siteId)
            if (latest.exists()) {
                latest.copyTo(baseline, overwrite = true)
                latest.delete()
            }
            logger.d { "Promoted latest to baseline for site $siteId" }
        }.onFailure { logger.e(it) { "Failed to promote latest for site $siteId" } }
    }

    suspend fun readBaselineHtml(siteId: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            baselineFile(siteId).readText()
        }.onFailure { logger.e(it) { "Failed to read baseline for site $siteId" } }
    }

    suspend fun readLatestHtml(siteId: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            latestFile(siteId).readText()
        }.onFailure { logger.e(it) { "Failed to read latest for site $siteId" } }
    }

    suspend fun deleteLatest(siteId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching<Unit> {
            latestFile(siteId).delete()
        }.onFailure { logger.e(it) { "Failed to delete latest for site $siteId" } }
    }

    suspend fun deleteAll(siteId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            baselineFile(siteId).delete()
            latestFile(siteId).delete()
            logger.d { "Deleted all HTML files for site $siteId" }
        }.onFailure { logger.e(it) { "Failed to delete HTML for site $siteId" } }
    }
}
