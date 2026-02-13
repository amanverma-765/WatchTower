package com.riva.watchtower.presentation.features.settings.logic

sealed class SettingsUiEvent {
    data class ToggleBackgroundCheck(val enabled: Boolean) : SettingsUiEvent()
    data class ChangeInterval(val minutes: Long) : SettingsUiEvent()
    data object NotificationPermissionGranted : SettingsUiEvent()
    data object NotificationPermissionDenied : SettingsUiEvent()
    data class ChangePoolSize(val size: Int) : SettingsUiEvent()
}
