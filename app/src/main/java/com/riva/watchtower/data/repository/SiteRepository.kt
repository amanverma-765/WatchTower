package com.riva.watchtower.data.repository

import co.touchlab.kermit.Logger
import com.riva.watchtower.data.db.SiteDao
import com.riva.watchtower.data.db.toDomain
import com.riva.watchtower.data.db.toEntity
import com.riva.watchtower.data.external.SiteTrackingProvider
import com.riva.watchtower.data.local.HtmlStorageProvider
import com.riva.watchtower.domain.enums.SiteStatus
import com.riva.watchtower.utils.HtmlContentExtractor
import com.riva.watchtower.domain.models.Site
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.UUID

class SiteRepository(
    private val siteDao: SiteDao,
    private val siteTracker: SiteTrackingProvider,
    private val htmlStorage: HtmlStorageProvider
) {

    companion object {
        private val logger = Logger.withTag("SiteRepository")
    }

    fun observeAllSites(): Flow<List<Site>> =
        siteDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun addSite(url: String): Result<Site> = runCatching {
        val html = siteTracker.fetchSiteHtml(url).getOrThrow()
        val domain = extractDomain(url)
        val favicon = "https://www.google.com/s2/favicons?domain=$domain&sz=64"
        val hash = md5(HtmlContentExtractor.extractVisibleText(html))
        val now = System.currentTimeMillis()
        val siteId = UUID.randomUUID().toString()

        val site = Site(
            id = siteId,
            name = domain,
            url = url,
            favicon = favicon,
            lastCheckedAt = now,
            createdAt = now,
            lastStatus = SiteStatus.PASSED,
            contentHash = hash
        )

        // Save as baseline — this is the accepted reference
        htmlStorage.saveBaseline(siteId, html).getOrThrow()
        siteDao.upsert(site.toEntity())
        logger.i { "Added site: $domain ($url)" }
        site
    }.onFailure { logger.e(it) { "Failed to add site: $url" } }

    suspend fun checkAllSites(): Result<List<Site>> = runCatching {
        val sites = siteDao.getAll().map { it.toDomain() }
        coroutineScope {
            sites.map { site ->
                async(Dispatchers.IO) {
                    checkSite(site)
                }
            }.awaitAll()
        }
    }.onFailure { logger.e(it) { "Failed to check all sites" } }

    suspend fun checkSite(site: Site): Site {
        return try {
            val html = siteTracker.fetchSiteHtml(site.url).getOrThrow()
            val newHash = md5(HtmlContentExtractor.extractVisibleText(html))
            val now = System.currentTimeMillis()

            // Compare against the baseline hash (contentHash), NOT the latest fetch
            val changed = newHash != site.contentHash

            if (changed) {
                // Save as latest — baseline stays untouched
                htmlStorage.saveLatest(site.id, html)
            } else {
                // Content matches baseline again — clean up any stale latest
                htmlStorage.deleteLatest(site.id)
            }

            // Only update status + timestamp, NEVER update contentHash here
            // contentHash only changes on resolve (when user accepts the change)
            val updated = site.copy(
                lastCheckedAt = now,
                lastStatus = if (changed) SiteStatus.CHANGED else SiteStatus.PASSED
            )
            siteDao.upsert(updated.toEntity())
            updated
        } catch (e: Exception) {
            logger.e(e) { "Failed to check site: ${site.url}" }
            val errorSite = site.copy(
                lastCheckedAt = System.currentTimeMillis(),
                lastStatus = SiteStatus.ERROR
            )
            siteDao.upsert(errorSite.toEntity())
            errorSite
        }
    }

    suspend fun getSiteById(id: String): Site? =
        siteDao.getById(id)?.toDomain()

    suspend fun getBaselineHtml(siteId: String): Result<String> =
        htmlStorage.readBaselineHtml(siteId)

    suspend fun getLatestHtml(siteId: String): Result<String> =
        htmlStorage.readLatestHtml(siteId)

    suspend fun deleteSite(id: String) {
        siteDao.deleteById(id)
        htmlStorage.deleteAll(id)
        logger.i { "Deleted site: $id" }
    }

    suspend fun resolveSite(id: String) {
        val entity = siteDao.getById(id) ?: return
        // Read the latest HTML to compute its hash as the new baseline hash
        val latestHtml = htmlStorage.readLatestHtml(id).getOrNull()
        val newHash = latestHtml?.let { md5(HtmlContentExtractor.extractVisibleText(it)) } ?: entity.contentHash
        // Promote latest → baseline
        htmlStorage.promoteLatestToBaseline(id)
        // Update status to PASSED and contentHash to the new baseline
        siteDao.upsert(entity.copy(
            lastStatus = SiteStatus.PASSED.name,
            contentHash = newHash
        ))
        logger.i { "Resolved site: $id — new baseline hash: $newHash" }
    }

    private fun extractDomain(url: String): String =
        url.removePrefix("https://").removePrefix("http://").removePrefix("www.").split("/").first()

    private fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
