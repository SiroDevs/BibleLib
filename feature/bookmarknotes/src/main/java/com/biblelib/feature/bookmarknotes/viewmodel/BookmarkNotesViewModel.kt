package com.biblelib.feature.bookmarknotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblelib.core.data.repos.AnnotationRepo
import com.biblelib.core.database.model.BookmarkEntity
import com.biblelib.core.database.model.NoteEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookmarkNotesUiState(
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val isLoading: Boolean = true,
    val selectedBookmarkKeys: Set<String> = emptySet(),
    val selectedNoteKeys: Set<String> = emptySet(),
) {
    val isBookmarkSelectionMode: Boolean get() = selectedBookmarkKeys.isNotEmpty()
    val isNoteSelectionMode: Boolean get() = selectedNoteKeys.isNotEmpty()
}

@HiltViewModel
class BookmarkNotesViewModel @Inject constructor(
    private val annotationRepo: AnnotationRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarkNotesUiState())
    val uiState: StateFlow<BookmarkNotesUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val bookmarks = annotationRepo.getAllBookmarks()
            val notes = annotationRepo.getAllNotes()
            _uiState.update {
                it.copy(bookmarks = bookmarks, notes = notes, isLoading = false)
            }
        }
    }

    private fun keyOf(abbr: String, verseId: String) = "$abbr|$verseId"

    fun toggleBookmarkSelection(item: BookmarkEntity) {
        val key = keyOf(item.bibleAbbr, item.verseId)
        _uiState.update {
            val s = it.selectedBookmarkKeys
            it.copy(selectedBookmarkKeys = if (key in s) s - key else s + key)
        }
    }

    fun toggleNoteSelection(item: NoteEntity) {
        val key = keyOf(item.bibleAbbr, item.verseId)
        _uiState.update {
            val s = it.selectedNoteKeys
            it.copy(selectedNoteKeys = if (key in s) s - key else s + key)
        }
    }

    fun clearBookmarkSelection() = _uiState.update { it.copy(selectedBookmarkKeys = emptySet()) }
    fun clearNoteSelection() = _uiState.update { it.copy(selectedNoteKeys = emptySet()) }

    fun deleteSelectedBookmarks() {
        val state = _uiState.value
        val toDelete = state.bookmarks.filter { keyOf(it.bibleAbbr, it.verseId) in state.selectedBookmarkKeys }
        viewModelScope.launch {
            annotationRepo.deleteBookmarks(toDelete)
            clearBookmarkSelection()
            load()
        }
    }

    fun deleteSelectedNotes() {
        val state = _uiState.value
        val toDelete = state.notes.filter { keyOf(it.bibleAbbr, it.verseId) in state.selectedNoteKeys }
        viewModelScope.launch {
            annotationRepo.deleteNotes(toDelete)
            clearNoteSelection()
            load()
        }
    }

    fun clearAllBookmarks() {
        viewModelScope.launch {
            annotationRepo.deleteBookmarks(_uiState.value.bookmarks)
            clearBookmarkSelection()
            load()
        }
    }

    fun clearAllNotes() {
        viewModelScope.launch {
            annotationRepo.deleteNotes(_uiState.value.notes)
            clearNoteSelection()
            load()
        }
    }
}
