package com.biblelib.feature.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.TrackingRepo
import com.biblelib.core.database.model.SearchEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bibleRepo: BibleRepo,
    private val prefsRepo: PrefsRepo,
    private val trackingRepo: TrackingRepo,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<VerseDisplay>>(emptyList())
    val results: StateFlow<List<VerseDisplay>> = _results.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<SearchEntity>>(emptyList())
    val searchHistory: StateFlow<List<SearchEntity>> = _searchHistory.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSearchHistory()
        // Debounced search
        viewModelScope.launch {
            _query
                .debounce(400)
                .distinctUntilChanged()
                .collect { q ->
                    if (q.length >= 3) performSearch(q) else _results.value = emptyList()
                }
        }
    }

    /** Bible whose local text was searched — needed to open a result in the reader. */
    val primaryBibleAbbr: String get() = prefsRepo.primaryBible

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun clearQuery() {
        _query.value = ""
        _results.value = emptyList()
    }

    private fun performSearch(q: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            try {
                val abbr = prefsRepo.primaryBible
                val results = bibleRepo.searchVerses(abbr, q)
                _results.value = results
                if (results.isNotEmpty()) {
                    trackingRepo.recordSearch(q)
                    loadSearchHistory()
                }
            } catch (e: Exception) {
                _results.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun searchFromHistory(q: String) {
        _query.value = q
        performSearch(q)
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            trackingRepo.clearSearchHistory()
            _searchHistory.value = emptyList()
        }
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            _searchHistory.value = trackingRepo.getSearchHistory()
        }
    }
}