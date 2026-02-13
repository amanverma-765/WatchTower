package com.riva.watchtower.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class AppDestinations {
    @Serializable
    data object Home : AppDestinations()

    @Serializable
    data class Detail(val siteId: String) : AppDestinations()

    @Serializable
    data object Alerts : AppDestinations()
}
