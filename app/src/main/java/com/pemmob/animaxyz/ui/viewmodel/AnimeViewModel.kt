package com.pemmob.animaxyz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.animaxyz.data.model.Genre
import com.pemmob.animaxyz.data.repository.AnimeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class AnimeViewModel(
    private val repository: AnimeRepository = AnimeRepository()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenreId = MutableStateFlow<Int?>(null)
    val selectedGenreId: StateFlow<Int?> = _selectedGenreId.asStateFlow()

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _detailUiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val detailUiState: StateFlow<DetailUiState> = _detailUiState.asStateFlow()

    private val retryHomeTrigger = MutableStateFlow(0)
    private var lastDetailId: Int? = null
    private var detailJob: Job? = null


    private var isFirstQuery = true
    private val debouncedQuery = _searchQuery.debounce {
        if (isFirstQuery) {
            isFirstQuery = false
            0L
        } else {
            500L
        }
    }

    init {
        loadGenres()
        observeSearch()
    }


    private fun loadGenres() {
        viewModelScope.launch {
            try {
                val genreList = repository.getGenres()
                _genres.value = genreList
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }


    private fun observeSearch() {
        viewModelScope.launch {
            val queryAndGenre = combine(debouncedQuery, _selectedGenreId) { query, genreId ->
                query to genreId
            }.distinctUntilChanged()

            combine(queryAndGenre, retryHomeTrigger) { (query, genreId), _ ->
                query to genreId
            }.collectLatest { (query, genreId) ->
                performSearch(query, genreId)
            }
        }
    }


    private suspend fun performSearch(query: String, genreId: Int?) {
        _homeUiState.value = HomeUiState.Loading
        try {
            val list = repository.searchAnime(query, genreId)
            _homeUiState.value = HomeUiState.Success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _homeUiState.value = HomeUiState.Error(mapErrorMessage(e))
        }
    }


    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }


    fun onGenreSelected(genreId: Int?) {
        _selectedGenreId.value = if (_selectedGenreId.value == genreId) null else genreId
    }


    fun retryHome() {
        retryHomeTrigger.value++
    }


    fun loadAnimeDetail(id: Int) {
        lastDetailId = id
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _detailUiState.value = DetailUiState.Loading
            try {
                val anime = repository.getAnimeDetail(id)
                _detailUiState.value = DetailUiState.Success(anime)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _detailUiState.value = DetailUiState.Error(mapErrorMessage(e))
            }
        }
    }


    fun retryDetail() {
        lastDetailId?.let { id ->
            loadAnimeDetail(id)
        }
    }


    private fun mapErrorMessage(throwable: Throwable): String {
        return when {
            throwable is HttpException && throwable.code() == 429 -> {
                "Terlalu banyak permintaan, coba sebentar lagi."
            }
            throwable is HttpException && throwable.code() == 404 -> {
                "Anime tidak ditemukan."
            }
            throwable is IOException -> {
                "Tidak ada koneksi internet. Periksa jaringan lalu coba lagi."
            }
            else -> {
                "Terjadi kesalahan. Silakan coba lagi."
            }
        }
    }
}
