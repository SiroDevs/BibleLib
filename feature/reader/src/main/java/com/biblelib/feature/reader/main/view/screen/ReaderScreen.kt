package com.biblelib.feature.reader.main.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.biblelib.core.common.utils.AppFonts
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.ThemeMode
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.designsystem.customization.AppFontFamilies
import com.biblelib.core.designsystem.customization.AppReaderBackgrounds
import com.biblelib.core.ui.components.indicators.ErrorState
import com.biblelib.core.ui.components.indicators.VerseShimmer
import com.biblelib.feature.reader.main.view.components.BibleSelectorSheet
import com.biblelib.feature.reader.main.view.components.BookDrawer
import com.biblelib.feature.reader.main.view.components.BookmarkOptionsDialog
import com.biblelib.feature.reader.main.view.components.ChapterNavBar
import com.biblelib.feature.reader.main.view.components.ChapterSheet
import com.biblelib.feature.reader.main.view.components.HighlightColorPickerDialog
import com.biblelib.feature.reader.main.view.components.ReaderSelectionTopBar
import com.biblelib.feature.reader.viewmodel.ReaderViewModel
import com.biblelib.feature.reader.main.view.components.ReaderTopBar
import com.biblelib.feature.reader.main.view.components.VerseList
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    navController: NavController,
    viewModel: ReaderViewModel,
    initialBible: String,
    initialBibleAbbr: String,
    initialBookId: String,
    initialChapterId: String,
    themeRepo: ThemeRepo,
) {
    LaunchedEffect(Unit) {
        viewModel.initialize(initialBible, initialBibleAbbr, initialBookId, initialChapterId)
    }

    val state by viewModel.uiState.collectAsState()
    var showBookDrawer by remember { mutableStateOf(false) }
    var showChapterSheet by remember { mutableStateOf(false) }
    var showBibleSelector by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val resolvedFontFamily = AppFontFamilies.byId(state.fontFamilyId).family
    val resolvedBackground = AppReaderBackgrounds.byId(state.readerBackgroundId)

    LaunchedEffect(state.activeChapter?.id, state.verses) {
        val target = state.restoreVerseId
        if (target != null && state.verses.isNotEmpty()) {
            val idx = state.verses.indexOfFirst { it.verseId == target }
            if (idx >= 0) listState.scrollToItem(idx)
            viewModel.consumeRestoreVerseTarget()
        }
    }

    LaunchedEffect(listState, state.verses) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .debounce(500)
            .collect { index ->
                val verse = state.verses.getOrNull(index) ?: return@collect
                viewModel.onVerseScrollPositionChanged(verse.verseId, verse.number)
            }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentViewModel = rememberUpdatedState(viewModel)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentViewModel.value.refreshNotedVerses()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.notesNavRequest) {
        val request = state.notesNavRequest ?: return@LaunchedEffect
        navController.navigate(
            Routes.notes(
                bibleAbbr = request.bibleAbbr,
                verseId = request.verseId,
                bookId = request.bookId,
                chapterId = request.chapterId,
                title = request.title,
                verseText = request.verseText,
            )
        )
        viewModel.consumeNotesNavRequest()
    }

    Scaffold(
        topBar = {
            if (state.isSelectionMode) {
                ReaderSelectionTopBar(
                    selectedCount = state.selectedVerseIds.size,
                    onClose = viewModel::clearSelection,
                    onBookmarkClick = viewModel::openColorPicker,
                    onNotesClick = { viewModel.openNotesForSelection() },
                )
            } else {
                ReaderTopBar(
                    bibleName = state.activeBible,
                    bookName = state.activeBook?.name ?: "",
                    onBibleClick = { showBibleSelector = true },
                    onBookClick = { showBookDrawer = true },
                    onSearchClick = { navController.navigate(Routes.SEARCH) },
                    onBookmarksNotesClick = { navController.navigate(Routes.BOOKMARKS_NOTES) },
                    onHistoryClick = { navController.navigate(Routes.HISTORY) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                    onSupportClick = { navController.navigate(Routes.DONATION) },
                    onHelpClick = { navController.navigate(Routes.HELP) },
                )
            }
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
        },
        floatingActionButton = {
            if (!state.isSelectionMode) {
                FloatingActionButton(onClick = { showQuickSettings = true }) {
                    Icon(Icons.Default.Tune, "Quick Settings")
                }
            }
        }
    ) { padding ->
        Box(Modifier
            .fillMaxSize()
            .padding(padding)
            .background(resolvedBackground.brush())) {
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
                    fontFamily = resolvedFontFamily,
                    listState = listState,
                    bookmarks = state.bookmarks,
                    notedVerseIds = state.notedVerseIds,
                    selectedVerseIds = state.selectedVerseIds,
                    isSelectionMode = state.isSelectionMode,
                    onLongPress = viewModel::toggleVerseSelected,
                    onTap = viewModel::toggleVerseSelected,
                    onSwipeBookmark = viewModel::quickToggleBookmark,
                    onSwipeNotes = { verseId -> viewModel.requestNotesForVerse(verseId) },
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

    if (state.showColorPicker) {
        HighlightColorPickerDialog(
            colors = ReaderViewModel.HIGHLIGHT_COLORS,
            onColorChosen = viewModel::chooseHighlightColor,
            onDismiss = viewModel::dismissColorPicker,
        )
    }

    if (state.pendingHighlightColor != null) {
        BookmarkOptionsDialog(
            onBookmarkOnly = viewModel::confirmBookmarkOnly,
            onBookmarkWithNotes = { viewModel.confirmBookmarkWithNotes() },
            onDismiss = viewModel::cancelPendingHighlight,
        )
    }

    if (showQuickSettings) {
        AlertDialog(
            onDismissRequest = { showQuickSettings = false },
            title = { Text("Quick Settings") },
            text = {
                Column {
                    Text(
                        "Theme",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeRepo.selectedTheme == mode,
                                onClick = { themeRepo.setTheme(mode) },
                                label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        "Font size: ${state.fontSizeSp.toInt()}sp",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Slider(
                        value = state.fontSizeSp,
                        onValueChange = viewModel::setFontSize,
                        valueRange = AppFonts.MIN_FONT_SP..AppFonts.MAX_FONT_SP,
                        steps = ((AppFonts.MAX_FONT_SP - AppFonts.MIN_FONT_SP) / 2).toInt() - 1,
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Multi-Bible Reader", style = MaterialTheme.typography.labelMedium)
                        Switch(
                            checked = state.multiBibleReaderEnabled,
                            onCheckedChange = viewModel::setMultiBibleReaderEnabled,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQuickSettings = false }) { Text("Done") }
            },
        )
    }
}
