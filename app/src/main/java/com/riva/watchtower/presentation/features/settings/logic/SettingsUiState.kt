package com.riva.watchtower.presentation.features.settings.logic

data class IntervalOption(
    val label: String,
    val minutes: Long
)

data class SettingsUiState(
    val isBackgroundCheckEnabled: Boolean = false,
    val intervalMinutes: Long = 180L,
    val intervalOptions: List<IntervalOption> = defaultIntervalOptions,
    val hasNotificationPermission: Boolean = false,
    val parallelPoolSize: Int = 10,
    val poolSizeOptions: List<Int> = defaultPoolSizeOptions
)

val defaultPoolSizeOptions = listOf(1, 3, 5, 10, 15, 20)

val defaultIntervalOptions = listOf(
    IntervalOption("15 min", 15L),
    IntervalOption("30 min", 30L),
    IntervalOption("1 hour", 60L),
    IntervalOption("3 hours", 180L),
    IntervalOption("6 hours", 360L),
    IntervalOption("12 hours", 720L),
    IntervalOption("24 hours", 1440L)
)
