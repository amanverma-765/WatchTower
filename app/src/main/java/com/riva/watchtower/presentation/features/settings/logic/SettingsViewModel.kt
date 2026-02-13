package com.riva.watchtower.presentation.features.settings.logic

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riva.watchtower.data.local.SettingsDataStore
import com.riva.watchtower.worker.WorkScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsDataStore.isBackgroundCheckEnabled.collect { enabled ->
                _uiState.update { it.copy(isBackgroundCheckEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.checkIntervalMinutes.collect { minutes ->
                _uiState.update { it.copy(intervalMinutes = minutes) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.parallelPoolSize.collect { size ->
                _uiState.update { it.copy(parallelPoolSize = size) }
            }
        }
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.ToggleBackgroundCheck -> toggleBackgroundCheck(event.enabled)
            is SettingsUiEvent.ChangeInterval -> changeInterval(event.minutes)
            is SettingsUiEvent.ChangePoolSize -> changePoolSize(event.size)
            SettingsUiEvent.NotificationPermissionGranted -> {
                _uiState.update { it.copy(hasNotificationPermission = true) }
            }
            SettingsUiEvent.NotificationPermissionDenied -> {
                _uiState.update { it.copy(hasNotificationPermission = false) }
                // Disable background check if permission denied
                toggleBackgroundCheck(false)
            }
        }
    }

    private fun toggleBackgroundCheck(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setBackgroundCheckEnabled(enabled)
            if (enabled) {
                WorkScheduler.schedulePeriodicCheck(appContext, _uiState.value.intervalMinutes)
            } else {
                WorkScheduler.cancelPeriodicCheck(appContext)
            }
        }
    }

    private fun changeInterval(minutes: Long) {
        viewModelScope.launch {
            settingsDataStore.setCheckIntervalMinutes(minutes)
            if (_uiState.value.isBackgroundCheckEnabled) {
                WorkScheduler.schedulePeriodicCheck(appContext, minutes)
            }
        }
    }

    private fun changePoolSize(size: Int) {
        viewModelScope.launch {
            settingsDataStore.setParallelPoolSize(size)
        }
    }
}
