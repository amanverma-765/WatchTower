package com.riva.watchtower.presentation.features.home.logic

import com.riva.watchtower.domain.models.Site

data class HomeUiState(
    val isLoading: Boolean = false,
    val isChecking: Boolean = false,
    val checkProgress: Float = 0f,
    val sites: List<Site> = emptyList(),
    val siteAddUrl: String = "",
    val siteAddUrlError: String? = null,
    val siteAddLoading: Boolean = false,
    val errorMessage: String? = null,
    val nextCheckAt: Long? = null,
    val isBackgroundCheckEnabled: Boolean = false
)
