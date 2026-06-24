package com.biblelib.feature.reader.view

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.biblelib.core.common.entity.UiState
import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.database.model.BookEntity
import com.biblelib.core.database.model.ChapterEntity
import com.biblelib.core.database.model.SavedBibleEntity
import com.biblelib.core.ui.components.indicators.ErrorState
import com.biblelib.core.ui.components.indicators.VerseShimmer
import com.biblelib.feature.reader.ReaderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    navController: NavController,
    viewModel: ReaderViewModel,
    initialBibleAbbr: String,
    initialBookId: String,
    initialChapterId: String,
    prefsRepo: PrefsRepo,
) {
    LaunchedEffect(Unit) {
        viewModel.initialize(initialBibleAbbr, initialBookId, initialChapterId)
    }

    val state by viewModel.uiState.collectAsState()
    var showBookDrawer    by remember { mutableStateOf(false) }
    var showChapterSheet  by remember { mutableStateOf(false) }
    var showBibleSelector by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ReaderTopBar(
                bibleAbbr    = state.activeBibleAbbr,
                bookName     = state.activeBook?.name ?: "",
                chapterRef   = state.activeChapter?.reference ?: "",
                savedBibles  = state.savedBibles,
                onBibleClick = { showBibleSelector = true },
                onBookClick  = { showBookDrawer = true },
                onSearchClick= { navController.navigate(Routes.SEARCH) },
                onHistoryClick={ navController.navigate(Routes.HISTORY) },
                onSettingsClick={ navController.navigate(Routes.SETTINGS) },
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
                chapterRef   = state.activeChapter?.reference ?: "",
                onPrev       = { viewModel.navigateChapter(-1) },
                onNext       = { viewModel.navigateChapter(1) },
                onChapterList= { showChapterSheet = true },
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> VerseShimmer()
                state.error != null -> ErrorState(
                    message = state.error!!,
                    onRetry = { viewModel.initialize(initialBibleAbbr, initialBookId, initialChapterId) }
                )
                else -> VerseList(
                    verses         = state.verses,
                    parallelVerses = state.parallelVerses,
                    fontSizeSp     = state.fontSizeSp,
                )
            }
        }
    }

    // ── Book drawer ──────────────────────────────────────────────────────────
    if (showBookDrawer) {
        BookDrawer(
            books       = state.books,
            activeBookId= state.activeBook?.id ?: "",
            onSelect    = { book ->
                viewModel.selectBook(book)
                showBookDrawer = false
            },
            onDismiss   = { showBookDrawer = false },
        )
    }

    // ── Chapter bottom sheet ─────────────────────────────────────────────────
    if (showChapterSheet) {
        ChapterSheet(
            chapters       = state.chapters,
            activeChapterId= state.activeChapter?.id ?: "",
            onSelect       = { ch ->
                viewModel.selectChapter(ch)
                showChapterSheet = false
            },
            onDismiss = { showChapterSheet = false },
        )
    }

    // ── Bible selector sheet ─────────────────────────────────────────────────
    if (showBibleSelector) {
        BibleSelectorSheet(
            savedBibles    = state.savedBibles,
            activeBibleAbbr= state.activeBibleAbbr,
            onSelect       = { abbr ->
                viewModel.selectBible(abbr)
                showBibleSelector = false
            },
            onDismiss = { showBibleSelector = false },
        )
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderTopBar(
    bibleAbbr: String,
    bookName: String,
    chapterRef: String,
    savedBibles: List<SavedBibleEntity>,
    onBibleClick: () -> Unit,
    onBookClick: () -> Unit,
    onSearchClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Column(
                modifier = Modifier
                    .clickable { onBookClick() }
                    .padding(vertical = 4.dp)
            ) {
                Text(bookName.ifEmpty { "BibleLib" }, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(chapterRef, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f))
            }
        },
        navigationIcon = {
            // Bible version button
            TextButton(onClick = onBibleClick) {
                Text(
                    text = bibleAbbr.uppercase().take(3),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, "Search", tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = onHistoryClick) {
                Icon(Icons.Default.History, "History", tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor    = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
        )
    )
}

// ─── Verse list ───────────────────────────────────────────────────────────────

@Composable
private fun VerseList(
    verses: List<VerseDisplay>,
    parallelVerses: Map<String, List<VerseDisplay>>,
    fontSizeSp: Float,
) {
    val hasParallel = parallelVerses.isNotEmpty()
    LazyColumn(
        contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(verses, key = { it.verseId }) { verse ->
            VerseRow(
                verse          = verse,
                fontSizeSp     = fontSizeSp,
                parallelTexts  = if (hasParallel) {
                    parallelVerses.mapValues { (_, pVerses) ->
                        pVerses.find { it.number == verse.number }?.text ?: ""
                    }
                } else emptyMap(),
            )
        }
    }
}

@Composable
private fun VerseRow(
    verse: VerseDisplay,
    fontSizeSp: Float,
    parallelTexts: Map<String, String>,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        // Primary verse
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = (fontSizeSp * 0.72f).sp,
                    color = MaterialTheme.colorScheme.primary,
                )) { append("${verse.number} ") }
                append(verse.text)
            },
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * 1.6f).sp,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // Parallel verses
        parallelTexts.forEach { (abbr, text) ->
            if (text.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = (fontSizeSp * 0.65f).sp,
                            color = MaterialTheme.colorScheme.secondary,
                        )) { append("[${abbr.uppercase()}] ") }
                        append(text)
                    },
                    fontSize = (fontSizeSp * 0.85f).sp,
                    lineHeight = (fontSizeSp * 1.5f).sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                )
            }
        }
    }
}

// ─── Chapter Nav Bar ──────────────────────────────────────────────────────────

@Composable
private fun ChapterNavBar(
    hasPrev: Boolean,
    hasNext: Boolean,
    chapterRef: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onChapterList: () -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
        NavigationBarItem(
            selected = false,
            onClick  = onPrev,
            enabled  = hasPrev,
            icon     = { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous") },
            label    = { Text("Prev") }
        )
        NavigationBarItem(
            selected = false,
            onClick  = onChapterList,
            icon     = { Icon(Icons.Default.MenuBook, "Chapters") },
            label    = { Text(chapterRef, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        )
        NavigationBarItem(
            selected = false,
            onClick  = onNext,
            enabled  = hasNext,
            icon     = { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next") },
            label    = { Text("Next") }
        )
    }
}

// ─── Book Drawer ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookDrawer(
    books: List<BookEntity>,
    activeBookId: String,
    onSelect: (BookEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Books",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
            items(books, key = { it.id }) { book ->
                ListItem(
                    headlineContent = { Text(book.nameLong.ifEmpty { book.name }) },
                    supportingContent = { Text(book.name) },
                    leadingContent = {
                        Text(
                            book.id.take(3),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    trailingContent = if (book.id == activeBookId) ({
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }) else null,
                    modifier = Modifier.clickable { onSelect(book) },
                )
                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}

// ─── Chapter Sheet ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterSheet(
    chapters: List<ChapterEntity>,
    activeChapterId: String,
    onSelect: (ChapterEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Chapters",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 56.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            items(chapters, key = { it.id }) { chapter ->
                val isActive = chapter.id == activeChapterId
                Surface(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelect(chapter) },
                    color = if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = chapter.number,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ─── Bible Selector Sheet ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BibleSelectorSheet(
    savedBibles: List<SavedBibleEntity>,
    activeBibleAbbr: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Switch Bible",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        savedBibles.forEach { bible ->
            ListItem(
                headlineContent = { Text(bible.name) },
                supportingContent = { Text(bible.abbreviation.uppercase()) },
                trailingContent = {
                    if (!bible.isDownloaded) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(4.dp))
                            Text("Downloading", style = MaterialTheme.typography.labelSmall)
                        }
                    } else if (bible.abbreviation == activeBibleAbbr) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                modifier = Modifier.clickable(enabled = bible.isDownloaded) {
                    onSelect(bible.abbreviation)
                },
            )
            HorizontalDivider(thickness = 0.5.dp)
        }
        Spacer(Modifier.height(32.dp))
    }
}
