package com.biblelib.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblelib.core.data.repos.AnnotationRepo
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.database.model.BibleEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepo: PrefsRepo,
    private val bibleRepo: BibleRepo,
    private val annotationRepo: AnnotationRepo,
) : ViewModel() {

    private val _savedBibles = MutableStateFlow<List<BibleEntity>>(emptyList())
    val savedBibles: StateFlow<List<BibleEntity>> = _savedBibles.asStateFlow()

    private val _fontSizeSp = MutableStateFlow(prefsRepo.fontSizeSp)
    val fontSizeSp: StateFlow<Float> = _fontSizeSp.asStateFlow()

    private val _multiBibleEnabled = MutableStateFlow(prefsRepo.multiBibleReaderEnabled)
    val multiBibleEnabled: StateFlow<Boolean> = _multiBibleEnabled.asStateFlow()

    /** Ordered stack of secondary Bibles (excludes the primary Bible). */
    private val _secondaryBibles = MutableStateFlow<List<String>>(emptyList())
    val secondaryBibles: StateFlow<List<String>> = _secondaryBibles.asStateFlow()

    private val _showClearAnnotationsDialog = MutableStateFlow(false)
    val showClearAnnotationsDialog: StateFlow<Boolean> = _showClearAnnotationsDialog.asStateFlow()

    init {
        loadBibles()
    }

    fun loadBibles() {
        viewModelScope.launch {
            val bibles = bibleRepo.getbibles()
            _savedBibles.value = bibles

            val owned = bibles.map { it.abbreviation }.toSet()
            val primary = prefsRepo.primaryBible

            var secondary = prefsRepo.getSecondaryBibleList().filter { it in owned && it != primary }
            if (secondary.isEmpty()) {
                secondary = bibles
                    .filter { it.abbreviation != primary }
                    .take(PrefsRepo.DEFAULT_SECONDARY_BIBLES)
                    .map { it.abbreviation }
            }
            prefsRepo.setSecondaryBibleList(secondary)
            _secondaryBibles.value = secondary
        }
    }

    fun setFontSize(sp: Float) {
        prefsRepo.fontSizeSp = sp
        _fontSizeSp.value = sp
    }

    // ───────────────────────── Multi-Bible Reader ─────────────────────────

    fun setMultiBibleEnabled(enabled: Boolean) {
        prefsRepo.multiBibleReaderEnabled = enabled
        _multiBibleEnabled.value = enabled
    }

    /** Toggles [abbr] in/out of the secondary stack, respecting the 1–5 secondary cap. */
    fun toggleSecondaryBible(abbr: String) {
        val current = _secondaryBibles.value
        val updated = if (abbr in current) {
            current - abbr
        } else {
            if (current.size >= PrefsRepo.MAX_SECONDARY_BIBLES) return
            current + abbr
        }
        _secondaryBibles.value = updated
        prefsRepo.setSecondaryBibleList(updated)
    }

    /** Moves [abbr] up (-1) or down (+1) in the secondary stack order. */
    fun moveSecondaryBible(abbr: String, direction: Int) {
        val list = _secondaryBibles.value.toMutableList()
        val idx = list.indexOf(abbr)
        val newIdx = idx + direction
        if (idx < 0 || newIdx !in list.indices) return
        val item = list.removeAt(idx)
        list.add(newIdx, item)
        _secondaryBibles.value = list
        prefsRepo.setSecondaryBibleList(list)
    }

    // ───────────────────────── Danger zone ─────────────────────────

    fun requestClearAnnotations() {
        _showClearAnnotationsDialog.value = true
    }

    fun dismissClearAnnotations() {
        _showClearAnnotationsDialog.value = false
    }

    fun confirmClearAnnotations() {
        viewModelScope.launch {
            annotationRepo.clearAllBookmarksAndNotes()
            _showClearAnnotationsDialog.value = false
        }
    }
}
