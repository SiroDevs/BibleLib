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
import com.biblelib.core.ui.MainViewModel
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.database.model.BookEntity
import com.biblelib.core.database.model.DraftEntity
import com.biblelib.core.database.model.ListingUi
import com.biblelib.core.database.model.SongEntity
import com.biblelib.feature.donation.DonationViewModel
import com.biblelib.feature.donation.view.DonationScreen
import com.biblelib.feature.donation.view.PaymentWebViewScreen
import com.biblelib.feature.edits.admin.AdminEditsViewModel
import com.biblelib.feature.edits.admin.view.AdminEditsScreen
import com.biblelib.feature.drafts.list.DraftsViewModel
import com.biblelib.feature.drafts.present.DraftPresenterViewModel
import com.biblelib.feature.drafts.list.view.DraftsScreen
import com.biblelib.feature.drafts.present.view.DraftPresenterScreen
import com.biblelib.feature.edits.user.EditsViewModel
import com.biblelib.feature.edits.user.view.EditsScreen
import com.biblelib.feature.help.view.HelpScreen
import com.biblelib.feature.history.HistoryViewModel
import com.biblelib.feature.history.view.HistoryScreen
import com.biblelib.feature.home.HomeViewModel
import com.biblelib.feature.home.view.HomeScreen
import com.biblelib.feature.howitworks.view.HowItWorksScreen
import com.biblelib.feature.listing.ListingViewModel
import com.biblelib.feature.listing.view.ListingScreen
import com.biblelib.feature.song.presentor.PresenterViewModel
import com.biblelib.feature.song.presentor.view.PresenterScreen
import com.biblelib.feature.selection.SelectionViewModel
import com.biblelib.feature.selection.view.SelectionScreen
import com.biblelib.feature.settings.SettingsViewModel
import com.biblelib.feature.settings.UserProfileViewModel
import com.biblelib.feature.settings.view.SettingsScreen
import com.biblelib.feature.settings.view.UserProfileScreen
import com.biblelib.feature.song.editor.EditorViewModel
import com.biblelib.feature.song.editor.view.EditorScreen
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
        is MainViewModel.Destination.Home -> Routes.HOME
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

        composable(Routes.HOME) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val mainViewModel: MainViewModel = hiltViewModel()
            HomeScreen(
                navController = navController,
                mainViewModel = mainViewModel,
                homeViewModel = homeViewModel,
                prefsRepo = prefsRepo
            )
        }

        composable(Routes.PRESENT) {
            val book = navController.previousBackStackEntry
                ?.savedStateHandle?.get<BookEntity>("book")
            val song = navController.previousBackStackEntry
                ?.savedStateHandle?.get<SongEntity>("song")
            val viewModel: PresenterViewModel = hiltViewModel()
            PresenterScreen(
                navController = navController,
                viewModel = viewModel,
                book = book,
                song = song,
                prefsRepo = prefsRepo
            )
        }

        composable(Routes.EDITOR) {
            val song  = navController.previousBackStackEntry
                ?.savedStateHandle?.get<SongEntity>("song_to_edit")
            val viewModel: EditorViewModel = hiltViewModel()
            if (song != null) {
                EditorScreen(
                    navController = navController,
                    song = song,
                    viewModel = viewModel,
                )
            } else {
                navController.popBackStack()
            }
        }

        composable(Routes.DRAFT_PRESENT) {
            val draft = navController.previousBackStackEntry
                ?.savedStateHandle?.get<DraftEntity>("draft")
            val horizontalSlides = prefsRepo.horizontalSlides
            val viewModel: DraftPresenterViewModel = hiltViewModel()
            if (draft != null) {
                DraftPresenterScreen(
                    navController = navController,
                    draft = draft,
                    horizontalSlides = horizontalSlides,
                    viewModel = viewModel,
                )
            } else {
                navController.popBackStack()
            }
        }

        composable(Routes.DRAFT_EDITOR) {
            val draft = navController.previousBackStackEntry
                ?.savedStateHandle?.get<DraftEntity>("draft_to_edit")
            val viewModel: EditorViewModel = hiltViewModel()
            if (draft != null) {
                EditorScreen(
                    navController = navController,
                    draft = draft,
                    viewModel = viewModel,
                )
            } else {
                navController.popBackStack()
            }
        }

        composable(Routes.LISTING) {
            val listing = navController.previousBackStackEntry
                ?.savedStateHandle?.get<ListingUi>("listing")
            val viewModel: ListingViewModel = hiltViewModel()
            ListingScreen(
                navController = navController,
                viewModel = viewModel,
                listing = listing,
                prefsRepo = prefsRepo
            )
        }

        composable(Routes.SETTINGS) {
            val mainViewModel: MainViewModel = hiltViewModel()
            val settViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                navController = navController,
                mainViewModel = mainViewModel,
                settViewModel = settViewModel,
                themeRepo = themeRepo,
            )
        }

        composable(Routes.HOW_IT_WORKS) {
            HowItWorksScreen(navController = navController)
        }

        composable(Routes.HELP) {
            HelpScreen(navController = navController)
        }

        composable(Routes.DONATION) {
            val viewModel: DonationViewModel = hiltViewModel()
            DonationScreen(
                navController = navController,
                viewModel = viewModel,
            )
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
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = false }
                        }
                    } else {
                        viewModel.resetState()
                        navController.popBackStack()
                    }
                },
            )
        }

        composable(Routes.DRAFTS) {
            val viewModel: DraftsViewModel = hiltViewModel()
            DraftsScreen(navController = navController, viewModel = viewModel)
        }

        composable(Routes.HISTORY) {
            val viewModel: HistoryViewModel = hiltViewModel()
            HistoryScreen(navController = navController, viewModel = viewModel)
        }

        composable(Routes.USER_PROFILE) {
            val viewModel: UserProfileViewModel = hiltViewModel()
            UserProfileScreen(
                navController = navController,
                viewModel = viewModel,
                onSignInRequested = {}
            )
        }

        composable(Routes.USER_EDITS) {
            val viewModel: EditsViewModel = hiltViewModel()
            EditsScreen(
                navController = navController,
                prefsRepo = prefsRepo,
                viewModel = viewModel,
            )
        }

        composable(Routes.ADMIN_EDITS) {
            val viewModel: AdminEditsViewModel = hiltViewModel()
            AdminEditsScreen(navController = navController, viewModel = viewModel)
        }
    }
}