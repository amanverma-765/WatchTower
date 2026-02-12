package com.riva.watchtower.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class AppDestinations {
    @Serializable
    data object Home: AppDestinations()

    @Serializable
    data object SiteAdd: AppDestinations()

    @Serializable
    data object Detail: AppDestinations()

    @Serializable
    data object Alerts: AppDestinations()
}