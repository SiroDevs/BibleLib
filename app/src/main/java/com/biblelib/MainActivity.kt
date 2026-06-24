package com.biblelib

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.biblelib.app.navigation.AppNavHost
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ThemeMode
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.designsystem.theme.AppTheme
import com.biblelib.core.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefsRepo: PrefsRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val mainViewModel: MainViewModel by viewModels()

        splashScreen.setKeepOnScreenCondition {
            !mainViewModel.isReady.value
        }

        setContent {
            val themeRepo: ThemeRepo = hiltViewModel()
            val themeMode = themeRepo.selectedTheme
            val isDarkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            AppTheme(useDarkTheme = isDarkTheme) {
                AppNavHost(
                    themeRepo = themeRepo,
                    prefsRepo = prefsRepo,
                    mainViewModel = mainViewModel
                )
            }
        }
    }
}
