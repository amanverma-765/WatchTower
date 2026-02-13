package com.riva.watchtower.presentation.features.home.logic

import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import co.touchlab.kermit.Logger
import com.riva.watchtower.data.local.SettingsDataStore
import com.riva.watchtower.domain.repository.SiteRepository
import com.riva.watchtower.utils.NotificationHelper
import com.riva.watchtower.utils.SiteCheckRunner
import com.riva.watchtower.worker.WorkScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: SiteRepository,
    private val settingsDataStore: SettingsDataStore,
    private val appContext: Context
) : ViewModel() {

    private val logger = Logger.withTag("HomeViewModel")
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeSites()
        observeBackgroundCheckState()
    }

    private fun observeSites() {
        viewModelScope.launch {
            repository.observeAllSites().collect { sites ->
                _uiState.update { it.copy(sites = sites, isLoading = false) }
            }
        }
    }

    private fun observeBackgroundCheckState() {
        viewModelScope.launch {
            settingsDataStore.isBackgroundCheckEnabled.collect { enabled ->
                _uiState.update { it.copy(isBackgroundCheckEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            WorkManager.getInstance(appContext)
                .getWorkInfosForUniqueWorkFlow(WorkScheduler.UNIQUE_WORK_NAME)
                .collect { workInfos ->
                    val nextRun = workInfos
                        .firstOrNull { it.state == WorkInfo.State.ENQUEUED }
                        ?.nextScheduleTimeMillis
                        ?.takeIf { it > 0 }
                    _uiState.update { it.copy(nextCheckAt = nextRun) }
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
            val sites = repository.getAllSites()
            if (sites.isEmpty()) return@launch

            val poolSize = settingsDataStore.parallelPoolSize.first()
            val notificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            _uiState.update { it.copy(isChecking = true, checkProgress = 0f) }

            val stats = SiteCheckRunner.checkAll(
                sites = sites,
                poolSize = poolSize,
                repository = repository,
                onProgress = { progress ->
                    _uiState.update {
                        it.copy(checkProgress = progress.completed.toFloat() / progress.total)
                    }
                    notificationManager.notify(
                        NotificationHelper.PROGRESS_NOTIFICATION_ID,
                        NotificationHelper.buildProgressNotification(
                            appContext, progress.completed, progress.total, progress.siteName
                        )
                    )
                }
            )

            notificationManager.cancel(NotificationHelper.PROGRESS_NOTIFICATION_ID)
            NotificationHelper.sendResultNotification(appContext, stats)

            _uiState.update { it.copy(isChecking = false, checkProgress = 0f) }
        }
    }
}
