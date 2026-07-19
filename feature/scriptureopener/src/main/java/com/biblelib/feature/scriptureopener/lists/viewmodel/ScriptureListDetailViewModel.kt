package com.biblelib.feature.scriptureopener.lists.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblelib.core.common.entity.ScriptureNavTarget
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ScriptureQueueRepo
import com.biblelib.core.data.repos.ScriptureRepo
import com.biblelib.core.database.model.ScriptureItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScriptureListDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val listId: Long = 0,
    val listName: String = "",
    val items: List<ScriptureItemEntity> = emptyList(),
    val navigateToReader: ScriptureNavTarget? = null,
)

@HiltViewModel
class ScriptureListDetailViewModel @Inject constructor(
    private val scriptureRepo: ScriptureRepo,
    private val scriptureQueueRepo: ScriptureQueueRepo,
    private val prefsRepo: PrefsRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScriptureListDetailUiState())
    val uiState: StateFlow<ScriptureListDetailUiState> = _uiState.asStateFlow()

    fun initialize(listId: Long) {
        if (_uiState.value.listId == listId && _uiState.value.items.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, listId = listId) }
            val list = scriptureRepo.getList(listId)
            val items = scriptureRepo.getItems(listId)
            if (list == null || items.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, error = "This scripture list could not be found.") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = false, listName = list.name, items = items) }
        }
    }

    fun rename(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        val listId = _uiState.value.listId
        viewModelScope.launch {
            scriptureRepo.renameList(listId, trimmed)
            _uiState.update { it.copy(listName = trimmed) }
        }
    }

    /** Opens this list in the reader, starting at its first scripture. */
    fun play() {
        val state = _uiState.value
        val first = state.items.firstOrNull() ?: return
        scriptureQueueRepo.open(state.listId, state.listName, state.items, activeItemId = first.id)
        prefsRepo.lastVerseId = first.verseId
        _uiState.update {
            it.copy(
                navigateToReader = ScriptureNavTarget(
                    bibleAbbr = first.bibleAbbr,
                    bibleName = first.bibleName,
                    bookId = first.bookId,
                    chapterId = first.chapterId,
                    verseId = first.verseId,
                )
            )
        }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(navigateToReader = null) }
    }
}
