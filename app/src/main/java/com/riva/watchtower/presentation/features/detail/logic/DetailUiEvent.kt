package com.riva.watchtower.presentation.features.detail.logic

sealed class DetailUiEvent {
    data object Recheck : DetailUiEvent()
    data object Delete : DetailUiEvent()
    data object MarkResolved : DetailUiEvent()
}
