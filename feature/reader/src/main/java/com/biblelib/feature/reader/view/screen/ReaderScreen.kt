package com.biblelib.feature.reader.view.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.ui.components.indicators.ErrorState
import com.biblelib.core.ui.components.indicators.VerseShimmer
import com.biblelib.feature.reader.viewmodel.ReaderViewModel
import com.biblelib.feature.reader.view.components.BibleSelectorSheet
import com.biblelib.feature.reader.view.components.BookDrawer
import com.biblelib.feature.reader.view.components.ChapterNavBar
import com.biblelib.feature.reader.view.components.ChapterSheet
import com.biblelib.feature.reader.view.components.ReaderTopBar
import com.biblelib.feature.reader.view.components.VerseList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    navController: NavController,
    viewModel: ReaderViewModel,
    initialBible: String,
    initialBibleAbbr: String,
    initialBookId: String,
    initialChapterId: String,
    prefsRepo: PrefsRepo,
) {
    LaunchedEffect(Unit) {
        viewModel.initialize(initialBible, initialBibleAbbr, initialBookId, initialChapterId)
    }

    val state by viewModel.uiState.collectAsState()
    var showBookDrawer by remember { mutableStateOf(false) }
    var showChapterSheet by remember { mutableStateOf(false) }
    var showBibleSelector by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ReaderTopBar(
                bibleName = state.activeBible,
                bookName = state.activeBook?.name ?: "",
                onBibleClick = { showBibleSelector = true },
                onBookClick = { showBookDrawer = true },
                onSearchClick = { navController.navigate(Routes.SEARCH) },
                onHistoryClick = { navController.navigate(Routes.HISTORY) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
            )
        },
        bottomBar = {
            ChapterNavBar(
                hasPrev = run {
                    val chapters = state.chapters
                    val idx = chapters.indexOfFirst { it.id == state.activeChapter?.id }
                    idx > 0
                },
                hasNext = run {
                    val chapters = state.chapters
                    val idx = chapters.indexOfFirst { it.id == state.activeChapter?.id }
                    idx < chapters.size - 1
                },
                chapterRef = state.activeChapter?.reference ?: "",
                onPrev = { viewModel.navigateChapter(-1) },
                onNext = { viewModel.navigateChapter(1) },
                onChapterList = { showChapterSheet = true },
            )
        }
    ) { padding ->
        Box(Modifier
            .fillMaxSize()
            .padding(padding)) {
            when {
                state.isLoading -> VerseShimmer()
                state.error != null -> ErrorState(
                    message = state.error!!,
                    onRetry = {
                        viewModel.initialize(
                            initialBible,
                            initialBibleAbbr,
                            initialBookId,
                            initialChapterId
                        )
                    }
                )

                else -> VerseList(
                    verses = state.verses,
                    parallelVerses = state.parallelVerses,
                    fontSizeSp = state.fontSizeSp,
                )
            }
        }
    }

    if (showBookDrawer) {
        BookDrawer(
            books = state.books,
            activeBookId = state.activeBook?.id ?: "",
            onSelect = { book ->
                viewModel.selectBook(book)
                showBookDrawer = false
            },
            onDismiss = { showBookDrawer = false },
        )
    }

    if (showChapterSheet) {
        ChapterSheet(
            chapters = state.chapters,
            activeChapterId = state.activeChapter?.id ?: "",
            onSelect = { ch ->
                viewModel.selectChapter(ch)
                showChapterSheet = false
            },
            onDismiss = { showChapterSheet = false },
        )
    }

    if (showBibleSelector) {
        BibleSelectorSheet(
            savedBibles = state.savedBibles,
            activeBibleAbbr = state.activeBibleAbbr,
            onSelect = { abbr ->
                viewModel.selectBible(abbr)
                showBibleSelector = false
            },
            onDismiss = { showBibleSelector = false },
        )
    }
}
