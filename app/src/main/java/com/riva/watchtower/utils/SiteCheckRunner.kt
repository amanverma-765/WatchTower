package com.riva.watchtower.utils

import co.touchlab.kermit.Logger
import com.riva.watchtower.domain.enums.SiteStatus
import com.riva.watchtower.domain.models.Site
import com.riva.watchtower.domain.repository.SiteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger

object SiteCheckRunner {

    private val logger = Logger.withTag("SiteCheckRunner")

    data class Progress(val completed: Int, val total: Int, val siteName: String)

    suspend fun checkAll(
        sites: List<Site>,
        poolSize: Int,
        repository: SiteRepository,
        onProgress: suspend (Progress) -> Unit
    ): CheckStats {
        val total = sites.size
        val semaphore = Semaphore(poolSize)
        val completed = AtomicInteger(0)
        val changedCount = AtomicInteger(0)
        val passedCount = AtomicInteger(0)
        val errorCount = AtomicInteger(0)

        coroutineScope {
            sites.map { site ->
                async {
                    semaphore.withPermit {
                        try {
                            val checked = repository.checkSite(site)
                            when (checked.lastStatus) {
                                SiteStatus.CHANGED -> changedCount.incrementAndGet()
                                SiteStatus.PASSED -> passedCount.incrementAndGet()
                                SiteStatus.ERROR -> errorCount.incrementAndGet()
                            }
                        } catch (e: Exception) {
                            errorCount.incrementAndGet()
                            logger.e(e) { "Failed to check site: ${site.url}" }
                        }

                        val done = completed.incrementAndGet()
                        onProgress(Progress(done, total, site.name))
                    }
                }
            }.awaitAll()
        }

        return CheckStats(
            total = total,
            changed = changedCount.get(),
            passed = passedCount.get(),
            error = errorCount.get()
        )
    }
}
