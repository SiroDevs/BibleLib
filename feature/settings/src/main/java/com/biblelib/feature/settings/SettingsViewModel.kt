package com.biblelib.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.worker.SyncScheduler
import com.biblelib.core.database.model.BibleEntity
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepo: PrefsRepo,
    private val bibleRepo: BibleRepo,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _savedBibles = MutableStateFlow<List<BibleEntity>>(emptyList())
    val savedBibles: StateFlow<List<BibleEntity>> = _savedBibles.asStateFlow()

    private val _fontSizeSp = MutableStateFlow(prefsRepo.fontSizeSp)
    val fontSizeSp: StateFlow<Float> = _fontSizeSp.asStateFlow()

    init { loadBibles() }

    fun loadBibles() {
        viewModelScope.launch { _savedBibles.value = bibleRepo.getbibles() }
    }

    fun setFontSize(sp: Float) {
        prefsRepo.fontSizeSp = sp
        _fontSizeSp.value = sp
    }

    fun removeBible(abbr: String) {
        viewModelScope.launch {
            SyncScheduler.cancelDownload(context, abbr)
            bibleRepo.deleteBible(abbr)
            val remaining = prefsRepo.getSelectedBibleList().filter { it != abbr }
            prefsRepo.selectedBibles = remaining.joinToString(",")
            if (prefsRepo.primaryBible == abbr) {
                prefsRepo.primaryBible = remaining.firstOrNull() ?: ""
            }
            loadBibles()
        }
    }

    fun requestReselection(mainViewModel: com.biblelib.core.ui.MainViewModel) {
        viewModelScope.launch {
            prefsRepo.selectAfresh = true
            mainViewModel.reset()
        }
    }
}
