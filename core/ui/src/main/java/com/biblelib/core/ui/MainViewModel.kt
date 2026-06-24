package com.biblelib.core.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.biblelib.core.data.repos.PrefsRepo
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefsRepo: PrefsRepo,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _destination = MutableStateFlow<Destination>(Destination.Reader)
    val destination: StateFlow<Destination> = _destination.asStateFlow()

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            if (prefsRepo.installDate == 0L) {
                prefsRepo.installDate = System.currentTimeMillis()
            }

            _destination.value = when {
                !prefsRepo.isDataSelected || prefsRepo.selectAfresh -> Destination.Selection
                else -> Destination.Reader
            }

            _isReady.value = true
        }
    }

    fun reset() {
        _isReady.value = false
        initializeApp()
    }

    sealed interface Destination {
        data object Reader    : Destination
        data object Selection : Destination
    }
}
