package com.biblelib.app.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.biblelib.feature.bibles.view.screen.BiblesScreen
import com.biblelib.feature.bibles.viewmodel.BiblesViewModel
import com.biblelib.feature.bookmarknotes.view.BookmarkNotesScreen
import com.biblelib.feature.bookmarknotes.viewmodel.BookmarkNotesViewModel
import com.biblelib.feature.donation.viewmodel.DonationViewModel
import com.biblelib.feature.donation.view.screen.DonationScreen
import com.biblelib.feature.donation.view.screen.PaymentWebViewScreen
import com.biblelib.feature.help.view.HelpScreen
import com.biblelib.feature.history.viewmodel.HistoryViewModel
import com.biblelib.feature.history.view.HistoryScreen
import com.biblelib.feature.reader.main.view.screen.ReaderScreen
import com.biblelib.feature.reader.notes.viewmodel.NotesViewModel
import com.biblelib.feature.reader.notes.view.NotesScreen
import com.biblelib.feature.reader.viewmodel.ReaderViewModel
import com.biblelib.feature.search.viewmodel.SearchViewModel
import com.biblelib.feature.search.view.screen.SearchScreen
import com.biblelib.feature.selection.viewmodel.SelectionViewModel
import com.biblelib.feature.selection.view.screen.SelectionScreen
import com.biblelib.feature.settings.viewmodel.SettingsViewModel
import com.biblelib.feature.settings.view.screen.SettingsScreen
import com.biblelib.feature.settings.view.screen.AppearanceSettingsScreen
import com.biblelib.feature.settings.view.screen.ReadingSettingsScreen
import com.biblelib.feature.settings.view.screen.DataSettingsScreen
import kotlinx.coroutines.launch

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

        composable(Routes.SELECTION) {
            val viewModel: SelectionViewModel = hiltViewModel()
            SelectionScreen(
                navController = navController,
                viewModel = viewModel,
                themeRepo = themeRepo,
            )
        }

        composable(
            route = Routes.READER,
            arguments = listOf(
                navArgument("bibleName") { type = NavType.StringType; defaultValue = "" },
                navArgument("bibleAbbr") { type = NavType.StringType; defaultValue = "" },
                navArgument("bookId") { type = NavType.StringType; defaultValue = "" },
                navArgument("chapterId") { type = NavType.StringType; defaultValue = "" },
            )
        ) { backStackEntry ->
            val bibleName = backStackEntry.arguments?.getString("bibleName") ?: ""
            val bibleAbbr = backStackEntry.arguments?.getString("bibleAbbr") ?: ""
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
            val viewModel: ReaderViewModel = hiltViewModel()
            ReaderScreen(
                navController = navController,
                viewModel = viewModel,
                initialBible = bibleName,
                initialBibleAbbr = bibleAbbr,
                initialBookId = bookId,
                initialChapterId = chapterId,
                themeRepo = themeRepo,
            )
        }

        composable(
            route = Routes.NOTES,
            arguments = listOf(
                navArgument("bibleAbbr") { type = NavType.StringType; defaultValue = "" },
                navArgument("verseId") { type = NavType.StringType; defaultValue = "" },
                navArgument("bookId") { type = NavType.StringType; defaultValue = "" },
                navArgument("chapterId") { type = NavType.StringType; defaultValue = "" },
                navArgument("title") { type = NavType.StringType; defaultValue = "" },
                navArgument("verseText") { type = NavType.StringType; defaultValue = "" },
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            val bibleAbbr = args?.getString("bibleAbbr") ?: ""
            val verseId = Routes.decode(args?.getString("verseId") ?: "")
            val bookId = Routes.decode(args?.getString("bookId") ?: "")
            val chapterId = Routes.decode(args?.getString("chapterId") ?: "")
            val title = Routes.decode(args?.getString("title") ?: "")
            val verseText = Routes.decode(args?.getString("verseText") ?: "")
            val viewModel: NotesViewModel = hiltViewModel()
            NotesScreen(
                navController = navController,
                viewModel = viewModel,
                bibleAbbr = bibleAbbr,
                verseId = verseId,
                bookId = bookId,
                chapterId = chapterId,
                title = title,
                verseText = verseText,
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
            SettingsScreen( navController = navController)
        }

        composable(Routes.BOOKMARKS_NOTES) {
            val viewModel: BookmarkNotesViewModel = hiltViewModel()
            BookmarkNotesScreen(
                navController = navController,
                viewModel = viewModel,
            )
        }

        composable(Routes.BIBLES) {
            val mainVm: MainViewModel = hiltViewModel()
            val biblesVm: BiblesViewModel = hiltViewModel()
            BiblesScreen(
                navController = navController,
                mainViewModel = mainVm,
                viewModel = biblesVm,
            )
        }

        composable(Routes.APPEARANCE_SETTINGS) {
            val settingsVm: SettingsViewModel = hiltViewModel()
            AppearanceSettingsScreen(
                navController = navController,
                settViewModel = settingsVm,
                themeRepo = themeRepo,
            )
        }

        composable(Routes.READING_SETTINGS) {
            val settingsVm: SettingsViewModel = hiltViewModel()
            ReadingSettingsScreen(
                navController = navController,
                settViewModel = settingsVm,
            )
        }

        composable(Routes.DATA_SETTINGS) {
            val mainVm: MainViewModel = hiltViewModel()
            val settingsVm: SettingsViewModel = hiltViewModel()
            DataSettingsScreen(
                navController = navController,
                mainViewModel = mainVm,
                settViewModel = settingsVm,
            )
        }

        composable(Routes.HELP) {
            HelpScreen(navController = navController)
        }

        composable(Routes.DONATION) {
            val donationVm: DonationViewModel = hiltViewModel()
            DonationScreen(navController = navController, viewModel = donationVm)
        }

        composable(
            route = Routes.PAYMENT_WEBVIEW,
            arguments = listOf(
                navArgument("redirectUrl") { type = NavType.StringType }
            ),
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("redirectUrl") ?: ""
            val redirectUrl = Routes.decodeRedirectUrl(encoded)

            val donationEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.DONATION)
            }
            val viewModel: DonationViewModel = hiltViewModel(donationEntry)
            val scope = rememberCoroutineScope()

            PaymentWebViewScreen(
                navController = navController,
                viewModel = viewModel,
                redirectUrl = redirectUrl,
                onPaymentComplete = { isSuccess ->
                    if (isSuccess) {
                        scope.launch { prefsRepo.recordDonation() }
                        navController.navigate(Routes.READER) {
                            popUpTo(Routes.READER) { inclusive = false }
                        }
                    } else {
                        viewModel.resetState()
                        navController.popBackStack()
                    }
                },
            )
        }

    }
}
