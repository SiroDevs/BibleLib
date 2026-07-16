package com.biblelib.feature.selection

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblelib.core.common.entity.Selectable
import com.biblelib.core.common.entity.UiState
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.worker.SyncScheduler
import com.biblelib.core.database.model.BibleEntity
import com.biblelib.core.network.dtos.BibleInfoDto
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectionViewModel @Inject constructor(
    private val bibleRepo: BibleRepo,
    private val prefsRepo: PrefsRepo,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    companion object {
        private const val TAG = "SelectionViewModel"
        const val MAX_SELECTIONS = 3
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _bibles = MutableStateFlow<List<Selectable<BibleInfoDto>>>(emptyList())
    val bibles = _bibles.asStateFlow()

    val selectedCount: StateFlow<Int> =
        _bibles
            .map { list -> list.count { it.isSelected } }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                0
            )

    val canProceed: StateFlow<Boolean> =
        selectedCount
            .map { it > 0 }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                false
            )

    fun fetchBibles() {
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val selected = prefsRepo.getSelectedBibleList().toSet()

                _bibles.value = bibleRepo.fetchAvailableBibles().map {
                    Selectable(
                        data = it,
                        isSelected = it.abbreviation in selected
                    )
                }

                _uiState.value = UiState.Loaded
            } catch (e: Exception) {
                Log.e(TAG, "fetchBibles", e)

                _uiState.value = UiState.Error(
                    "Could not load Bibles. Please check your connection and try again."
                )
            }
        }
    }

    fun toggleSelection(abbr: String) {
        val current = _bibles.value

        val target = current.find {
            it.data.abbreviation == abbr
        } ?: return

        val shouldSelect = !target.isSelected

        if (shouldSelect && selectedCount.value >= MAX_SELECTIONS) {
            return
        }

        _bibles.value = current.map {
            if (it.data.abbreviation == abbr) {
                it.copy(isSelected = shouldSelect)
            } else {
                it
            }
        }
    }

    fun saveSelectionAndDownload() {
        val selected = _bibles.value
            .filter { it.isSelected }
            .map { it.data }

        if (selected.isEmpty()) return

        _uiState.value = UiState.Saving

        viewModelScope.launch {
            try {

                val primary = selected.first()

                prefsRepo.selectedBibles =
                    selected.joinToString(",") { it.abbreviation }

                prefsRepo.primaryBible = primary.abbreviation
                prefsRepo.isDataSelected = true
                prefsRepo.selectAfresh = false

                prefsRepo.lastBibleAbbr = primary.abbreviation
                prefsRepo.lastBookId = ""
                prefsRepo.lastChapterId = ""

                bibleRepo.saveBibles(
                    selected.mapIndexed { index, dto ->
                        BibleEntity(
                            abbreviation = dto.abbreviation,
                            name = dto.name,
                            description = dto.description,
                            languageName = dto.language.name,
                            scriptDirection = dto.language.scriptDirection,
                            copyright = dto.copyright,
                            sortOrder = index,
                            isDownloaded = false
                        )
                    }
                )

                bibleRepo.downloadBible(primary.abbreviation)

                prefsRepo.isPrimaryLoaded = true

                selected.drop(1).forEach {
                    SyncScheduler.scheduleSecondaryDownload(
                        context,
                        it.abbreviation
                    )
                }

                _uiState.value = UiState.Saved

            } catch (e: Exception) {

                Log.e(TAG, "saveSelection", e)

                _uiState.value = UiState.Error(
                    "Failed to download the Bible. Please try again."
                )
            }
        }
    }
}