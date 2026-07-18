package com.biblelib.feature.reader.main.view.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSelectionTopBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onBookmarkClick: () -> Unit,
    onNotesClick: () -> Unit,
) {
    TopAppBar(
        title = { Text("$selectedCount selected") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "Cancel selection")
            }
        },
        actions = {
            IconButton(onClick = onBookmarkClick) {
                Icon(Icons.Default.Bookmark, "Bookmark")
            }
            IconButton(onClick = onNotesClick, enabled = selectedCount == 1) {
                Icon(Icons.Default.EditNote, "Notes")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    )
}
