package com.biblelib.feature.bookmarknotes.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.database.model.BookmarkEntity
import com.biblelib.core.database.model.NoteEntity
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.core.ui.components.general.ConfirmDialog
import com.biblelib.feature.bookmarknotes.viewmodel.BookmarkNotesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BookmarkNotesScreen(
    navController: NavController,
    viewModel: BookmarkNotesViewModel,
) {
    val state by viewModel.uiState.collectAsState()
    var activeTab by remember { mutableIntStateOf(0) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    val isSelectionMode = if (activeTab == 0) state.isBookmarkSelectionMode else state.isNoteSelectionMode
    val selectedCount = if (activeTab == 0) state.selectedBookmarkKeys.size else state.selectedNoteKeys.size

    fun openInReader(bibleAbbr: String, bookId: String, chapterId: String) {
        navController.navigate(
            Routes.reader(bibleAbbr = bibleAbbr, bookId = bookId, chapterId = chapterId)
        ) {
            popUpTo(Routes.READER) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (isSelectionMode) "$selectedCount selected" else "Bookmarks & Notes",
                showGoBack = true,
                onNavIconClick = {
                    if (isSelectionMode) {
                        if (activeTab == 0) viewModel.clearBookmarkSelection() else viewModel.clearNoteSelection()
                    } else {
                        navController.popBackStack()
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { showDeleteSelectedDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete selected")
                        }
                    } else {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, "More")
                        }
                        DropdownMenu(expanded = showOverflowMenu, onDismissRequest = { showOverflowMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(if (activeTab == 0) "Clear all bookmarks" else "Clear all notes") },
                                leadingIcon = { Icon(Icons.Default.DeleteSweep, null) },
                                onClick = { showOverflowMenu = false; showClearAllDialog = true },
                            )
                        }
                    }
                },
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Bookmarks") },
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Notes") },
                )
            }

            when (activeTab) {
                0 -> BookmarksTab(
                    bookmarks = state.bookmarks,
                    isLoading = state.isLoading,
                    selectedKeys = state.selectedBookmarkKeys,
                    isSelectionMode = state.isBookmarkSelectionMode,
                    onTap = { item ->
                        if (state.isBookmarkSelectionMode) {
                            viewModel.toggleBookmarkSelection(item)
                        } else {
                            openInReader(item.bibleAbbr, item.bookId, item.chapterId)
                        }
                    },
                    onLongPress = viewModel::toggleBookmarkSelection,
                )
                1 -> NotesTab(
                    notes = state.notes,
                    isLoading = state.isLoading,
                    selectedKeys = state.selectedNoteKeys,
                    isSelectionMode = state.isNoteSelectionMode,
                    onTap = { item ->
                        if (state.isNoteSelectionMode) {
                            viewModel.toggleNoteSelection(item)
                        } else {
                            openInReader(item.bibleAbbr, item.bookId, item.chapterId)
                        }
                    },
                    onLongPress = viewModel::toggleNoteSelection,
                )
            }
        }
    }

    if (showDeleteSelectedDialog) {
        ConfirmDialog(
            title = "Delete selected?",
            message = "This will permanently delete $selectedCount selected item(s). This can't be undone.",
            onConfirm = {
                if (activeTab == 0) viewModel.deleteSelectedBookmarks() else viewModel.deleteSelectedNotes()
                showDeleteSelectedDialog = false
            },
            onDismiss = { showDeleteSelectedDialog = false },
        )
    }

    if (showClearAllDialog) {
        ConfirmDialog(
            title = if (activeTab == 0) "Clear all bookmarks?" else "Clear all notes?",
            message = "This will permanently delete all your " +
                    (if (activeTab == 0) "bookmarks" else "notes") + " across every Bible. This can't be undone.",
            onConfirm = {
                if (activeTab == 0) viewModel.clearAllBookmarks() else viewModel.clearAllNotes()
                showClearAllDialog = false
            },
            onDismiss = { showClearAllDialog = false },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookmarksTab(
    bookmarks: List<BookmarkEntity>,
    isLoading: Boolean,
    selectedKeys: Set<String>,
    isSelectionMode: Boolean,
    onTap: (BookmarkEntity) -> Unit,
    onLongPress: (BookmarkEntity) -> Unit,
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (bookmarks.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No bookmarks yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
        return
    }
    LazyColumn {
        items(bookmarks, key = { "${it.bibleAbbr}|${it.verseId}" }) { item ->
            val key = "${item.bibleAbbr}|${item.verseId}"
            val selected = key in selectedKeys
            ListItem(
                headlineContent = { Text("${item.bookId} — ${item.chapterId.substringAfter(".")}") },
                supportingContent = { Text(item.bibleAbbr.uppercase(), style = MaterialTheme.typography.labelSmall) },
                leadingContent = {
                    Icon(
                        Icons.Default.Bookmark,
                        null,
                        tint = item.colorHex?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
                            ?: MaterialTheme.colorScheme.primary,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = { onTap(item) }, onLongClick = { onLongPress(item) }),
                colors = if (selected) {
                    androidx.compose.material3.ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                } else androidx.compose.material3.ListItemDefaults.colors(),
            )
            HorizontalDivider(thickness = 0.5.dp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotesTab(
    notes: List<NoteEntity>,
    isLoading: Boolean,
    selectedKeys: Set<String>,
    isSelectionMode: Boolean,
    onTap: (NoteEntity) -> Unit,
    onLongPress: (NoteEntity) -> Unit,
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (notes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No notes yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
        return
    }
    LazyColumn {
        items(notes, key = { "${it.bibleAbbr}|${it.verseId}" }) { item ->
            val key = "${item.bibleAbbr}|${item.verseId}"
            val selected = key in selectedKeys
            ListItem(
                headlineContent = { Text(item.title.ifBlank { item.verseText.take(40) }) },
                supportingContent = {
                    Text(
                        item.noteText,
                        maxLines = 2,
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.StickyNote2, null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = { onTap(item) }, onLongClick = { onLongPress(item) }),
                colors = if (selected) {
                    androidx.compose.material3.ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                } else androidx.compose.material3.ListItemDefaults.colors(),
            )
            HorizontalDivider(thickness = 0.5.dp)
        }
    }
}
