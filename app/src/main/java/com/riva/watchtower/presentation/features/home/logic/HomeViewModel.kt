package com.riva.watchtower.presentation.features.home.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.riva.watchtower.data.repository.SiteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: SiteRepository) : ViewModel() {

    private val logger = Logger.withTag("HomeViewModel")
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeSites()
    }

    private fun observeSites() {
        viewModelScope.launch {
            repository.observeAllSites().collect { sites ->
                _uiState.update { it.copy(sites = sites, isLoading = false) }
            }
        }
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnSiteAddUrlChange -> validateUrl(event.query)
            is HomeUiEvent.AddNewSite -> saveNewUrl(event.url)
            HomeUiEvent.ClearAddSite -> _uiState.update {
                it.copy(siteAddUrl = "", siteAddUrlError = null)
            }
            HomeUiEvent.CheckAllSites -> checkAllSites()
            HomeUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun validateUrl(url: String) {
        val urlError = when {
            url.isBlank() -> "URL cannot be empty"
            !url.startsWith("http://") && !url.startsWith("https://") -> "URL must start with http:// or https://"
            else -> null
        }
        _uiState.update { it.copy(siteAddUrlError = urlError, siteAddUrl = url) }
    }

    private fun saveNewUrl(url: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(siteAddLoading = true) }
            repository.addSite(url)
                .onSuccess {
                    logger.i { "Site added: ${it.name}" }
                    _uiState.update { state ->
                        state.copy(siteAddLoading = false, siteAddUrl = "", siteAddUrlError = null)
                    }
                }
                .onFailure { error ->
                    logger.e(error) { "Failed to add site" }
                    _uiState.update { state ->
                        state.copy(
                            siteAddLoading = false,
                            errorMessage = error.message ?: "Failed to add site"
                        )
                    }
                }
        }
    }

    private fun checkAllSites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true) }
            repository.checkAllSites()
                .onFailure { error ->
                    logger.e(error) { "Failed to check sites" }
                    _uiState.update { it.copy(errorMessage = error.message ?: "Failed to check sites") }
                }
            _uiState.update { it.copy(isChecking = false) }
        }
    }
}
