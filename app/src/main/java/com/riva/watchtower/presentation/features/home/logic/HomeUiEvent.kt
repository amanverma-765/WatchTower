package com.riva.watchtower.presentation.features.home.logic

sealed class HomeUiEvent {
    data class OnSiteAddUrlChange(val query: String): HomeUiEvent()
    data object ClearAddSite: HomeUiEvent()
    data class AddNewSite(val url: String): HomeUiEvent()
}