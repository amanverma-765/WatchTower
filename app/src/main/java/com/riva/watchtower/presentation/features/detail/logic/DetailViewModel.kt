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
            val oldBody = Ksoup.parse(baselineRaw).body().html()
            val newBody = Ksoup.parse(latestRaw).body().html()

            val oldLines = oldBody.lines()
            val newLines = newBody.lines()
            val patch = DiffUtils.diff(oldLines, newLines)
            if (patch.deltas.isEmpty()) return@withContext null

            // Collect line ranges that are new or changed in the latest body
            val changedLineNumbers = mutableSetOf<Int>()
            for (delta in patch.deltas) {
                if (delta.type == DeltaType.INSERT || delta.type == DeltaType.CHANGE) {
                    val start = delta.target.position
                    for (i in 0 until delta.target.lines.size) {
                        changedLineNumbers.add(start + i)
                    }
                }
            }

            // Build only changed lines grouped into labeled blocks with separators
            val changeCount = changedLineNumbers.size
            val sortedChanged = changedLineNumbers.sorted()
            val body = StringBuilder()
            var prevLine = -2
            var inBlock = false

            for (lineNum in sortedChanged) {
                // Non-contiguous change — close previous block, add separator
                if (lineNum > prevLine + 1 && prevLine >= 0) {
                    if (inBlock) {
                        body.appendLine("</div>")
                        inBlock = false
                    }
                    body.appendLine("""<div class="wt-sep"></div>""")
                }
                if (!inBlock) {
                    body.appendLine("""<div class="wt-block">""")
                    body.appendLine("""<span class="wt-label">MODIFIED</span>""")
                    inBlock = true
                }
                body.appendLine(newLines[lineNum])
                prevLine = lineNum
            }
            if (inBlock) body.appendLine("</div>")

            // Use full <head> from latest so all styles/resources resolve
            val latestDoc = Ksoup.parse(latestRaw)
            val headContent = latestDoc.head().html()

            //language=HTML
            """
                <!DOCTYPE html>
                <html><head>
                <meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1">
                $headContent
                <style>
                    /* ── Mobile overrides: tame the site's desktop CSS ── */
                    body {
                        max-width: 100vw !important;
                        overflow-x: hidden !important;
                        padding: 0 !important;
                        margin: 0 !important;
                    }
                    img, video, iframe, table, pre, code, svg {
                        max-width: 100% !important;
                        height: auto !important;
                    }
                    * {
                        max-width: 100vw !important;
                        box-sizing: border-box !important;
                        word-wrap: break-word !important;
                    }

                    /* ── WatchTower diff UI ── */
                    .wt-banner {
                        position: sticky; top: 0; z-index: 9999;
                        background: #1a1a1a; color: #e0e0e0;
                        font-family: system-ui, sans-serif;
                        font-size: 13px; line-height: 1.3;
                        padding: 12px 16px;
                        border-bottom: 2px solid #FF9800;
                    }
                    .wt-banner-row {
                        display: flex; align-items: center; gap: 8px;
                    }
                    .wt-banner-icon {
                        font-size: 16px; flex-shrink: 0;
                    }
                    .wt-banner-text {
                        font-size: 13px;
                    }
                    .wt-banner-text b {
                        color: #FFB74D;
                    }
                    .wt-banner-count {
                        background: rgba(255,152,0,0.2);
                        color: #FFB74D;
                        font-size: 11px; font-weight: 600;
                        padding: 2px 8px; border-radius: 10px;
                        margin-left: auto; white-space: nowrap;
                    }
                    .wt-block {
                        background: rgba(255, 152, 0, 0.06);
                        border-left: 3px solid #FF9800;
                        margin: 8px 0;
                        padding: 8px 12px;
                        border-radius: 0 8px 8px 0;
                    }
                    .wt-label {
                        display: inline-block;
                        background: #FF9800; color: #fff;
                        font-family: system-ui, sans-serif;
                        font-size: 10px; font-weight: 700;
                        letter-spacing: 0.5px;
                        padding: 3px 10px; border-radius: 10px;
                        margin-bottom: 8px;
                    }
                    .wt-sep {
                        margin: 24px 16px;
                        text-align: center;
                        color: #bbb; font-size: 12px;
                        font-family: system-ui, sans-serif;
                        position: relative;
                    }
                    .wt-sep::before {
                        content: '';
                        position: absolute; top: 50%; left: 0; right: 0;
                        border-top: 1px dashed #ddd;
                    }
                    .wt-sep::after {
                        content: 'unchanged';
                        position: relative;
                        background: #fafafa;
                        padding: 0 12px;
                        font-size: 11px;
                    }
                </style>
                </head><body>
                <div class="wt-banner">
                    <div class="wt-banner-row">
                        <span class="wt-banner-icon">&#9998;</span>
                        <span class="wt-banner-text"><b>Modified</b> sections highlighted</span>
                        <span class="wt-banner-count">$changeCount lines</span>
                    </div>
                </div>
                $body
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
