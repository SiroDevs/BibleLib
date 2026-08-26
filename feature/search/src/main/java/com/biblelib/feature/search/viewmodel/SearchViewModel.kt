package com.biblelib.feature.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.TrackingRepo
import com.biblelib.core.database.entities.BibleEntity
import com.biblelib.core.database.entities.SearchEntity
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

    /** All downloaded Bibles the user can search across, for the filter strip. */
    private val _bibles = MutableStateFlow<List<BibleEntity>>(emptyList())
    val bibles: StateFlow<List<BibleEntity>> = _bibles.asStateFlow()

    /** Bible currently being searched — defaults to the primary Bible. */
    private val _selectedBibleAbbr = MutableStateFlow("")
    val selectedBibleAbbr: StateFlow<String> = _selectedBibleAbbr.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadBibles()
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

    private fun loadBibles() {
        viewModelScope.launch {
            val downloaded = bibleRepo.getbibles().filter { it.isDownloaded }
            _bibles.value = downloaded
            val primary = prefsRepo.primaryBible
            _selectedBibleAbbr.value = when {
                downloaded.any { it.abbreviation == primary } -> primary
                else -> downloaded.firstOrNull()?.abbreviation ?: ""
            }
        }
    }

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun clearQuery() {
        _query.value = ""
        _results.value = emptyList()
    }

    /** Task: lets the user switch which downloaded Bible the search runs against. */
    fun selectBible(abbr: String) {
        if (abbr == _selectedBibleAbbr.value) return
        _selectedBibleAbbr.value = abbr
        val q = _query.value
        if (q.length >= 3) performSearch(q)
    }

    private fun performSearch(q: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            try {
                val abbr = _selectedBibleAbbr.value.ifEmpty { prefsRepo.primaryBible }
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