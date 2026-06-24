package com.biblelib.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.biblelib.core.data.repos.TrackingRepo
import com.biblelib.core.database.model.HistoryEntity
import com.biblelib.core.database.model.SearchEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class HistoryUiState(
    val readingHistory: List<HistoryGroup> = emptyList(),
    val searchHistory: List<SearchEntity>  = emptyList(),
    val isLoading: Boolean = true,
)

data class HistoryGroup(
    val dateLabel: String,
    val entries: List<HistoryEntity>,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val trackingRepo: TrackingRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init { loadHistory() }

    fun loadHistory() {
        viewModelScope.launch {
            val reading = trackingRepo.getReadingHistory()
            val searches = trackingRepo.getSearchHistory()
            val grouped = groupByDate(reading)
            _uiState.value = HistoryUiState(
                readingHistory = grouped,
                searchHistory  = searches,
                isLoading      = false,
            )
        }
    }

    fun clearReadingHistory() {
        viewModelScope.launch {
            trackingRepo.clearHistory()
            loadHistory()
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            trackingRepo.clearSearchHistory()
            loadHistory()
        }
    }

    private fun groupByDate(entries: List<HistoryEntity>): List<HistoryGroup> {
        val now = System.currentTimeMillis()
        val groups = mutableMapOf<String, MutableList<HistoryEntity>>()
        val fmt = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

        entries.forEach { entry ->
            val label = when {
                isToday(entry.readAt, now)     -> "Today"
                isYesterday(entry.readAt, now) -> "Yesterday"
                else -> fmt.format(Date(entry.readAt))
            }
            groups.getOrPut(label) { mutableListOf() }.add(entry)
        }

        // Preserve insertion order (already sorted by readAt DESC from DB)
        return groups.map { (label, items) -> HistoryGroup(label, items) }
    }

    private fun isToday(ts: Long, now: Long): Boolean {
        val dayMs = TimeUnit.DAYS.toMillis(1)
        return (now - ts) < dayMs && Date(now).date == Date(ts).date
    }

    private fun isYesterday(ts: Long, now: Long): Boolean {
        val dayMs = TimeUnit.DAYS.toMillis(1)
        val twoDayMs = TimeUnit.DAYS.toMillis(2)
        return (now - ts) in dayMs until twoDayMs
    }
}
