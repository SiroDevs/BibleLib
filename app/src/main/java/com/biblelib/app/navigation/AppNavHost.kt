package com.biblelib.app.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.ui.MainViewModel
import com.biblelib.feature.donation.DonationViewModel
import com.biblelib.feature.donation.view.DonationScreen
import com.biblelib.feature.help.view.HelpScreen
import com.biblelib.feature.history.HistoryViewModel
import com.biblelib.feature.history.view.HistoryScreen
import com.biblelib.feature.reader.ReaderViewModel
import com.biblelib.feature.reader.view.ReaderScreen
import com.biblelib.feature.search.SearchViewModel
import com.biblelib.feature.search.view.SearchScreen
import com.biblelib.feature.selection.SelectionViewModel
import com.biblelib.feature.selection.view.SelectionScreen
import com.biblelib.feature.settings.SettingsViewModel
import com.biblelib.feature.settings.view.SettingsScreen

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    themeRepo: ThemeRepo,
    prefsRepo: PrefsRepo,
    mainViewModel: MainViewModel,
) {
    val isReady by mainViewModel.isReady.collectAsState()
    val destination by mainViewModel.destination.collectAsState()

    if (!isReady) return

    val startDestination = when (destination) {
        is MainViewModel.Destination.Selection -> Routes.SELECTION
        is MainViewModel.Destination.Reader -> Routes.READER
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ─── Bible Selection ──────────────────────────────────────────────
        composable(Routes.SELECTION) {
            val viewModel: SelectionViewModel = hiltViewModel()
            SelectionScreen(
                navController = navController,
                viewModel = viewModel,
                themeRepo = themeRepo,
            )
        }

        // ─── Reader (main reading screen) ─────────────────────────────────
        composable(
            route = Routes.READER,
            arguments = listOf(
                navArgument("bibleAbbr") { type = NavType.StringType; defaultValue = "" },
                navArgument("bookId") { type = NavType.StringType; defaultValue = "" },
                navArgument("chapterId") { type = NavType.StringType; defaultValue = "" },
            )
        ) { backStackEntry ->
            val bibleAbbr = backStackEntry.arguments?.getString("bibleAbbr") ?: ""
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
            val viewModel: ReaderViewModel = hiltViewModel()
            ReaderScreen(
                navController = navController,
                viewModel = viewModel,
                initialBibleAbbr = bibleAbbr,
                initialBookId = bookId,
                initialChapterId = chapterId,
                prefsRepo = prefsRepo,
            )
        }

        composable(Routes.SEARCH) {
            val viewModel: SearchViewModel = hiltViewModel()
            SearchScreen(
                navController = navController,
                viewModel = viewModel,
            )
        }

        composable(Routes.HISTORY) {
            val viewModel: HistoryViewModel = hiltViewModel()
            HistoryScreen(
                navController = navController,
                viewModel = viewModel,
            )
        }

        composable(Routes.SETTINGS) {
            val mainVm: MainViewModel = hiltViewModel()
            val settingsVm: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                navController = navController,
                mainViewModel = mainVm,
                settViewModel = settingsVm,
                themeRepo = themeRepo,
            )
        }

        composable(Routes.HELP) {
            HelpScreen(navController = navController)
        }

        composable(Routes.DONATION) {
            val donationVm: DonationViewModel = hiltViewModel()
            DonationScreen(navController = navController, viewModel = donationVm)
        }
    }
}
