package com.biblelib.feature.settings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblelib.core.data.repos.AnnotationRepo
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.TrackingRepo
import com.biblelib.core.data.worker.SyncScheduler
import com.biblelib.core.ui.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ClearTarget { BOOKMARKS, NOTES, HISTORY, SEARCHES, ALL_DATA }

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepo: PrefsRepo,
    private val bibleRepo: BibleRepo,
    private val annotationRepo: AnnotationRepo,
    private val trackingRepo: TrackingRepo,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _fontSizeSp = MutableStateFlow(prefsRepo.fontSizeSp)
    val fontSizeSp: StateFlow<Float> = _fontSizeSp.asStateFlow()

    private val _readerFontFamilyId = MutableStateFlow(prefsRepo.readerFontFamily)
    val readerFontFamilyId: StateFlow<String> = _readerFontFamilyId.asStateFlow()

    private val _readerBackgroundId = MutableStateFlow(prefsRepo.readerBackground)
    val readerBackgroundId: StateFlow<String> = _readerBackgroundId.asStateFlow()

    private val _pendingClear = MutableStateFlow<ClearTarget?>(null)
    val pendingClear: StateFlow<ClearTarget?> = _pendingClear.asStateFlow()

    fun setFontSize(sp: Float) {
        prefsRepo.fontSizeSp = sp
        _fontSizeSp.value = sp
    }

    fun setReaderFontFamily(id: String) {
        prefsRepo.readerFontFamily = id
        _readerFontFamilyId.value = id
    }

    fun setReaderBackground(id: String) {
        prefsRepo.readerBackground = id
        _readerBackgroundId.value = id
    }

    // ───────────────────────── Data / Danger zone ─────────────────────────

    fun requestClear(target: ClearTarget) {
        _pendingClear.value = target
    }

    fun dismissClear() {
        _pendingClear.value = null
    }

    /** Confirms whatever clear action is pending. [mainViewModel] is only needed for ALL_DATA,
     *  which restarts the app back at the Selection screen. */
    fun confirmClear(mainViewModel: MainViewModel? = null) {
        val target = _pendingClear.value ?: return
        viewModelScope.launch {
            when (target) {
                ClearTarget.BOOKMARKS -> annotationRepo.deleteBookmarks(annotationRepo.getAllBookmarks())
                ClearTarget.NOTES -> annotationRepo.deleteNotes(annotationRepo.getAllNotes())
                ClearTarget.HISTORY -> trackingRepo.clearHistory()
                ClearTarget.SEARCHES -> trackingRepo.clearSearchHistory()
                ClearTarget.ALL_DATA -> {
                    SyncScheduler.cancelAll(context)
                    annotationRepo.clearAllBookmarksAndNotes()
                    trackingRepo.clearHistory()
                    trackingRepo.clearSearchHistory()
                    bibleRepo.deleteAllData()
                    prefsRepo.resetAppData()
                    mainViewModel?.reset()
                }
            }
            _pendingClear.value = null
        }
    }
}
