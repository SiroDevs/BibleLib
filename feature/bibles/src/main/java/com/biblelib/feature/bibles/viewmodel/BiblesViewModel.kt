package com.biblelib.feature.bibles.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.worker.SyncScheduler
import com.biblelib.core.data.worker.SyncWorker
import com.biblelib.core.database.model.BibleEntity
import com.biblelib.core.ui.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BiblesUiState(
    val bibles: List<BibleEntity> = emptyList(),
    val primaryAbbr: String = "",
    val downloadProgress: Map<String, Float> = emptyMap(),
    val pendingDelete: BibleEntity? = null,
    val showPrimaryPicker: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class BiblesViewModel @Inject constructor(
    private val bibleRepo: BibleRepo,
    private val prefsRepo: PrefsRepo,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)
    private val observedAbbrs = mutableSetOf<String>()

    private val _uiState = MutableStateFlow(BiblesUiState())
    val uiState: StateFlow<BiblesUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            val bibles = bibleRepo.getbibles().sortedBy { it.sortOrder }
            _uiState.update {
                it.copy(bibles = bibles, primaryAbbr = prefsRepo.primaryBible, isLoading = false)
            }
            observeDownloads(bibles)
        }
    }

    private fun observeDownloads(bibles: List<BibleEntity>) {
        bibles
            .filter { !it.isDownloaded && it.abbreviation !in observedAbbrs }
            .forEach { bible ->
                observedAbbrs += bible.abbreviation
                viewModelScope.launch {
                    workManager
                        .getWorkInfosForUniqueWorkFlow("${SyncWorker.WORK_NAME_PREFIX}${bible.abbreviation}")
                        .collect { infos ->
                            val info = infos.firstOrNull()
                            val progress = info?.progress?.getFloat(SyncWorker.KEY_PROGRESS, 0f) ?: 0f
                            _uiState.update {
                                it.copy(downloadProgress = it.downloadProgress + (bible.abbreviation to progress))
                            }
                            if (info != null && info.state.isFinished) {
                                observedAbbrs -= bible.abbreviation
                                load()
                            }
                        }
                }
            }
    }

    fun openPrimaryPicker() = _uiState.update { it.copy(showPrimaryPicker = true) }
    fun dismissPrimaryPicker() = _uiState.update { it.copy(showPrimaryPicker = false) }

    fun setPrimaryBible(abbr: String) {
        val bible = _uiState.value.bibles.find { it.abbreviation == abbr } ?: return
        if (!bible.isDownloaded) return

        prefsRepo.primaryBible = abbr
        prefsRepo.lastBibleAbbr = abbr
        prefsRepo.lastBookId = ""
        prefsRepo.lastChapterId = ""
        // A Bible can't be both primary and a secondary at the same time.
        prefsRepo.setSecondaryBibleList(prefsRepo.getSecondaryBibleList() - abbr)

        _uiState.update { it.copy(primaryAbbr = abbr, showPrimaryPicker = false) }
    }

    fun requestDelete(bible: BibleEntity) = _uiState.update { it.copy(pendingDelete = bible) }
    fun cancelDelete() = _uiState.update { it.copy(pendingDelete = null) }

    fun confirmDelete() {
        val bible = _uiState.value.pendingDelete ?: return
        viewModelScope.launch {
            SyncScheduler.cancelDownload(context, bible.abbreviation)
            bibleRepo.deleteBible(bible.abbreviation)

            val remaining = prefsRepo.getSelectedBibleList() - bible.abbreviation
            prefsRepo.selectedBibles = remaining.joinToString(",")
            prefsRepo.setSecondaryBibleList(prefsRepo.getSecondaryBibleList() - bible.abbreviation)

            if (prefsRepo.primaryBible == bible.abbreviation) {
                prefsRepo.primaryBible = remaining.firstOrNull() ?: ""
            }

            _uiState.update { it.copy(pendingDelete = null) }
            load()
        }
    }

    /** Restarts the Selection flow so the user can add/remove owned Bibles from scratch. */
    fun requestReselection(mainViewModel: MainViewModel) {
        viewModelScope.launch {
            prefsRepo.selectAfresh = true
            mainViewModel.reset()
        }
    }
}
