package com.biblelib.feature.selection

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.biblelib.core.common.entity.Selectable
import com.biblelib.core.common.entity.UiState
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.worker.SyncScheduler
import com.biblelib.core.database.model.BibleEntity
import com.biblelib.core.network.dtos.BibleInfoDto
import javax.inject.Inject

@HiltViewModel
class SelectionViewModel @Inject constructor(
    private val bibleRepo: BibleRepo,
    private val prefsRepo: PrefsRepo,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _bibles = MutableStateFlow<List<Selectable<BibleInfoDto>>>(emptyList())
    val bibles: StateFlow<List<Selectable<BibleInfoDto>>> = _bibles.asStateFlow()

    val maxSelections = 3
    val selectedCount get() = _bibles.value.count { it.isSelected }

    fun fetchBibles() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val available = bibleRepo.fetchAvailableBibles()
                val alreadySelected = prefsRepo.getSelectedBibleList().toSet()
                _bibles.value = available.map { dto ->
                    Selectable(dto, dto.abbreviation in alreadySelected)
                }
                _uiState.value = UiState.Loaded
            } catch (e: Exception) {
                Log.e(TAG, "fetchBibles error: ${e.message}", e)
                _uiState.value =
                    UiState.Error("Could not load Bible versions. Please check your connection and try again.")
            }
        }
    }

    fun toggleSelection(abbr: String) {
        val current = _bibles.value
        val target = current.find { it.data.abbreviation == abbr } ?: return
        val isNowSelected = !target.isSelected

        // Enforce max 3
        if (isNowSelected && selectedCount >= maxSelections) return

        _bibles.value = current.map {
            if (it.data.abbreviation == abbr) it.copy(isSelected = isNowSelected) else it
        }
    }

    fun canProceed(): Boolean = selectedCount >= 1

    fun saveSelectionAndDownload() {
        val selected = _bibles.value.filter { it.isSelected }.map { it.data }
        if (selected.isEmpty()) return

        _uiState.value = UiState.Saving
        viewModelScope.launch {
            try {
                val primary = selected.first()

                // Persist to prefs
                prefsRepo.selectedBibles = selected.joinToString(",") { it.abbreviation }
                prefsRepo.primaryBible = primary.abbreviation
                prefsRepo.isDataSelected = true
                prefsRepo.selectAfresh = false

                // Reset last position so reader opens at genesis
                prefsRepo.lastBibleAbbr = primary.abbreviation
                prefsRepo.lastBookId = ""
                prefsRepo.lastChapterId = ""

                // Save BibleEntity records
                val entities = selected.mapIndexed { i, dto ->
                    BibleEntity(
                        abbreviation = dto.abbreviation,
                        name = dto.name,
                        description = dto.description,
                        languageName = dto.language.name,
                        scriptDirection = dto.language.scriptDirection,
                        copyright = dto.copyright,
                        sortOrder = i,
                        isDownloaded = false,
                    )
                }
                bibleRepo.saveBibles(entities)

                // Download primary in-process (foreground — caller shows loading)
                bibleRepo.downloadBible(primary.abbreviation)
                prefsRepo.isPrimaryLoaded = true

                // Schedule secondary downloads in background with WorkManager
                selected.drop(1).forEach { dto ->
                    SyncScheduler.scheduleSecondaryDownload(context, dto.abbreviation)
                    Log.d(TAG, "Scheduled background download: ${dto.abbreviation}")
                }

                Log.d(TAG, "Selection saved. Primary=${primary.abbreviation}")
                _uiState.value = UiState.Saved
            } catch (e: Exception) {
                Log.e(TAG, "saveSelection error: ${e.message}", e)
                _uiState.value = UiState.Error("Failed to download the Bible. Please try again.")
            }
        }
    }

    companion object {
        private const val TAG = "SelectionViewModel"
    }
}
