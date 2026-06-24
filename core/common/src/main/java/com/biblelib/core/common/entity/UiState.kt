package com.biblelib.core.common.entity

sealed interface UiState {
    data object Loading : UiState
    data object Loaded  : UiState
    data object Saving  : UiState
    data object Saved   : UiState
    data class  Error(val message: String) : UiState
}
