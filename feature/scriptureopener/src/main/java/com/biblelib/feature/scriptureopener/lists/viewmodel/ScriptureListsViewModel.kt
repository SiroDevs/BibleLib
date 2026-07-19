package com.biblelib.feature.scriptureopener.lists.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblelib.core.data.repos.ScriptureRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScriptureListSummary(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val itemCount: Int,
)

data class ScriptureListsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val lists: List<ScriptureListSummary> = emptyList(),
)

@HiltViewModel
class ScriptureListsViewModel @Inject constructor(
    private val scriptureRepo: ScriptureRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScriptureListsUiState())
    val uiState: StateFlow<ScriptureListsUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                scriptureRepo.getAllLists().map { list ->
                    ScriptureListSummary(
                        id = list.id,
                        name = list.name,
                        createdAt = list.createdAt,
                        itemCount = scriptureRepo.getItemCount(list.id),
                    )
                }
            }.onSuccess { lists ->
                _uiState.update { it.copy(isLoading = false, lists = lists) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load scripture lists.") }
            }
        }
    }

    fun deleteList(listId: Long) {
        viewModelScope.launch {
            scriptureRepo.deleteList(listId)
            load()
        }
    }
}
