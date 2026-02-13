package com.riva.watchtower.domain.repository

import com.riva.watchtower.domain.models.Site
import kotlinx.coroutines.flow.Flow

interface SiteRepository {
    fun observeAllSites(): Flow<List<Site>>
    suspend fun addSite(url: String): Result<Site>
    suspend fun getAllSites(): List<Site>
    suspend fun checkSite(site: Site): Site
    suspend fun getSiteById(id: String): Site?
    suspend fun getBaselineHtml(siteId: String): Result<String>
    suspend fun getLatestHtml(siteId: String): Result<String>
    suspend fun deleteSite(id: String)
    suspend fun resolveSite(id: String)
}
