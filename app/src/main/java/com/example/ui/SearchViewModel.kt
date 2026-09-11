package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Song
import com.example.network.YouTubeSearchClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val songs: List<Song>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(
    private val searchClient: YouTubeSearchClient = YouTubeSearchClient()
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var debounceJob: Job? = null

    val quickChips = listOf(
        "Trending",
        "Top Hits 2024",
        "Lo-Fi Beats",
        "Acoustic Pop",
        "Synthwave",
        "Chillhop",
        "Classic Rock",
        "Peaceful Piano"
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        debounceJob?.cancel()

        val trimmed = newQuery.trim()
        if (trimmed.isEmpty()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        // Actively search on YouTube after 400ms debounce as user types
        debounceJob = viewModelScope.launch {
            delay(400)
            executeSearch(trimmed)
        }
    }

    fun search(targetQuery: String = _query.value) {
        debounceJob?.cancel()
        val trimmed = targetQuery.trim()
        if (trimmed.isEmpty()) return

        _query.value = trimmed
        viewModelScope.launch {
            executeSearch(trimmed)
        }
    }

    private suspend fun executeSearch(queryStr: String) {
        _uiState.value = SearchUiState.Loading
        val result = searchClient.searchSongs(queryStr)
        if (result.isSuccess) {
            val songs = result.getOrThrow()
            if (songs.isEmpty()) {
                _uiState.value = SearchUiState.Error("No tracks found on YouTube for '$queryStr'")
            } else {
                _uiState.value = SearchUiState.Success(songs)
            }
        } else {
            val err = result.exceptionOrNull()?.message ?: "Search failed on YouTube"
            _uiState.value = SearchUiState.Error(err)
        }
    }

    fun clearSearch() {
        debounceJob?.cancel()
        _query.value = ""
        _uiState.value = SearchUiState.Idle
    }
}

