package com.riva.watchtower.presentation.features.home.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.riva.watchtower.data.external.SiteTrackingProvider
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(private val siteTracker: SiteTrackingProvider) : ViewModel() {

    private val logger = Logger.withTag("HomeViewModel")
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnSiteAddUrlChange -> validateUrl(event.query)
            is HomeUiEvent.AddNewSite -> saveNewUrl(event.url)
            HomeUiEvent.ClearAddSite -> _uiState.update {
                it.copy(
                    siteAddUrl = "",
                    siteAddUrlError = null
                )
            }
        }
    }

    private fun validateUrl(url: String) {
        viewModelScope.launch {
            val urlError = when {
                url.isBlank() -> "URL cannot be empty"
                !url.startsWith("http://") && !url.startsWith("https://") -> "URL must start with http:// or https://"
                else -> null
            }
            _uiState.update { it.copy(siteAddUrlError = urlError, siteAddUrl = url) }
        }
    }

    private fun saveNewUrl(url: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(siteAddLoading = true) }
            siteTracker.fetchSiteHtml()
        }
    }
}