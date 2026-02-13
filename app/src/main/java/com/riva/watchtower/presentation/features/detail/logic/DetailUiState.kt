package com.riva.watchtower.presentation.features.detail.logic

import com.riva.watchtower.domain.models.Site

data class DetailUiState(
    val isLoading: Boolean = true,
    val site: Site? = null,
    val changedHtml: String? = null,
    val hasDiff: Boolean = false,
    val errorMessage: String? = null
)
