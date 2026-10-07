package com.pemmob.animaxyz.ui.viewmodel

import com.pemmob.animaxyz.data.model.Anime

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val animeList: List<Anime>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val anime: Anime) : DetailUiState
    data class Error(val message: String) : DetailUiState
}
