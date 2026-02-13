package com.riva.watchtower.presentation.features.detail.logic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import co.touchlab.kermit.Logger
import com.fleeksoft.ksoup.Ksoup
import com.github.difflib.DiffUtils
import com.github.difflib.patch.DeltaType
import com.riva.watchtower.domain.repository.SiteRepository
import com.riva.watchtower.domain.enums.SiteStatus
import com.riva.watchtower.presentation.navigation.AppDestinations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: SiteRepository
) : ViewModel() {

    companion object {
        private val logger = Logger.withTag("DetailViewModel")
    }

    private val siteId: String = savedStateHandle.toRoute<AppDestinations.Detail>().siteId
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSiteDetail()
    }

    fun onEvent(event: DetailUiEvent) {
        when (event) {
            DetailUiEvent.Recheck -> recheckSite()
            DetailUiEvent.Delete -> deleteSite()
            DetailUiEvent.MarkResolved -> markResolved()
        }
    }

    private fun loadSiteDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val site = repository.getSiteById(siteId)
            if (site == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Site not found") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = false, site = site) }
            if (site.lastStatus == SiteStatus.CHANGED) {
                buildChangedHtml()
            }
        }
    }

    private suspend fun buildChangedHtml() {
        val baselineRaw = repository.getBaselineHtml(siteId).getOrNull() ?: return
        val latestRaw = repository.getLatestHtml(siteId).getOrNull() ?: return

        val html = withContext(Dispatchers.Default) {
            // Extract only <body> inner HTML — strips <head>, <script>, <style>, meta noise
            val oldBody = Ksoup.parse(baselineRaw).body().html()
            val newBody = Ksoup.parse(latestRaw).body().html()

            val patch = DiffUtils.diff(oldBody.lines(), newBody.lines())
            if (patch.deltas.isEmpty()) return@withContext null

            // Collect only the new/changed body lines
            val changedLines = StringBuilder()
            for (delta in patch.deltas) {
                if (delta.type == DeltaType.INSERT || delta.type == DeltaType.CHANGE) {
                    delta.target.lines.forEach { changedLines.appendLine(it) }
                }
            }

            // Pull the original <head> styles so the rendered fragments look correct
            val headStyles = Ksoup.parse(latestRaw).head().select("style, link[rel=stylesheet]").outerHtml()

            """
                <!DOCTYPE html>
                <html><head>
                <meta name="viewport" content="width=device-width,initial-scale=1">
                $headStyles
                <style>
                    * { box-sizing: border-box; }
                    body { font-family: -apple-system, sans-serif;
                           margin: 0; padding: 12px; background: #fafafa; }
                </style>
                </head><body>
                $changedLines
                </body></html>
            """.trimIndent()
        } ?: return

        _uiState.update { it.copy(changedHtml = html, hasDiff = true) }
    }

    private fun recheckSite() {
        viewModelScope.launch {
            val site = _uiState.value.site ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            val updated = repository.checkSite(site)
            _uiState.update {
                it.copy(isLoading = false, site = updated, changedHtml = null, hasDiff = false)
            }
            if (updated.lastStatus == SiteStatus.CHANGED) {
                buildChangedHtml()
            }
        }
    }

    private fun deleteSite() {
        viewModelScope.launch {
            repository.deleteSite(siteId)
        }
    }

    private fun markResolved() {
        viewModelScope.launch {
            repository.resolveSite(siteId)
            val site = repository.getSiteById(siteId)
            _uiState.update { it.copy(site = site, changedHtml = null, hasDiff = false) }
        }
    }
}
