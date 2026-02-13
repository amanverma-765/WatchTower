package com.riva.watchtower.data.repository

import co.touchlab.kermit.Logger
import com.riva.watchtower.data.db.SiteDao
import com.riva.watchtower.data.db.toDomain
import com.riva.watchtower.data.db.toEntity
import com.riva.watchtower.data.external.SiteTrackingProvider
import com.riva.watchtower.data.local.HtmlStorageProvider
import com.riva.watchtower.domain.enums.SiteStatus
import com.riva.watchtower.domain.repository.SiteRepository
import com.riva.watchtower.utils.HtmlContentExtractor
import com.riva.watchtower.utils.UrlUtils
import com.riva.watchtower.domain.models.Site
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class SiteRepositoryImpl(
    private val siteDao: SiteDao,
    private val siteTracker: SiteTrackingProvider,
    private val htmlStorage: HtmlStorageProvider
) : SiteRepository {

    companion object {
        private val logger = Logger.withTag("SiteRepository")
    }

    override fun observeAllSites(): Flow<List<Site>> =
        siteDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addSite(url: String): Result<Site> = runCatching {
        val html = siteTracker.fetchSiteHtml(url).getOrThrow()
        val domain = UrlUtils.extractDomain(url)
        val favicon = UrlUtils.faviconUrl(domain)
        val hash = HtmlContentExtractor.contentHash(html)
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

    override suspend fun getAllSites(): List<Site> =
        siteDao.getAll().map { it.toDomain() }

    override suspend fun checkSite(site: Site): Site {
        return try {
            val html = siteTracker.fetchSiteHtml(site.url).getOrThrow()
            val newHash = HtmlContentExtractor.contentHash(html)
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

    override suspend fun getSiteById(id: String): Site? =
        siteDao.getById(id)?.toDomain()

    override suspend fun getBaselineHtml(siteId: String): Result<String> =
        htmlStorage.readBaselineHtml(siteId)

    override suspend fun getLatestHtml(siteId: String): Result<String> =
        htmlStorage.readLatestHtml(siteId)

    override suspend fun deleteSite(id: String) {
        siteDao.deleteById(id)
        htmlStorage.deleteAll(id)
        logger.i { "Deleted site: $id" }
    }

    override suspend fun resolveSite(id: String) {
        val entity = siteDao.getById(id) ?: return
        // Read the latest HTML to compute its hash as the new baseline hash
        val latestHtml = htmlStorage.readLatestHtml(id).getOrNull()
        val newHash = latestHtml?.let { HtmlContentExtractor.contentHash(it) } ?: entity.contentHash
        // Promote latest → baseline
        htmlStorage.promoteLatestToBaseline(id)
        // Update status to PASSED and contentHash to the new baseline
        siteDao.upsert(entity.copy(
            lastStatus = SiteStatus.PASSED.name,
            contentHash = newHash
        ))
        logger.i { "Resolved site: $id — new baseline hash: $newHash" }
    }

}
