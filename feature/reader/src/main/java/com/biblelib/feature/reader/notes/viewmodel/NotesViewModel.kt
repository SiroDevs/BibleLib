package com.biblelib.feature.reader.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblelib.core.data.repos.AnnotationRepo
import com.biblelib.core.database.model.NoteEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val isLoading: Boolean = true,
    val bibleAbbr: String = "",
    val verseId: String = "",
    val bookId: String = "",
    val chapterId: String = "",
    val title: String = "",
    val verseText: String = "",
    val noteText: String = "",
    val isSaved: Boolean = true,
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val annotationRepo: AnnotationRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    fun initialize(
        bibleAbbr: String,
        verseId: String,
        bookId: String,
        chapterId: String,
        title: String,
        verseText: String,
    ) {
        _uiState.update {
            it.copy(
                bibleAbbr = bibleAbbr,
                verseId = verseId,
                bookId = bookId,
                chapterId = chapterId,
                title = title,
                verseText = verseText,
            )
        }

        viewModelScope.launch {
            val existing = annotationRepo.getNote(bibleAbbr, verseId)
            _uiState.update {
                it.copy(
                    noteText = existing?.noteText ?: "",
                    isLoading = false,
                    isSaved = true,
                )
            }
        }
    }

    fun updateNoteText(text: String) {
        _uiState.update { it.copy(noteText = text, isSaved = false) }
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            annotationRepo.saveNote(
                NoteEntity(
                    verseId = state.verseId,
                    bibleAbbr = state.bibleAbbr,
                    bookId = state.bookId,
                    chapterId = state.chapterId,
                    title = state.title,
                    verseText = state.verseText,
                    noteText = state.noteText,
                )
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
