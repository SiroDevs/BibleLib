package com.biblelib.feature.reader.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.model.BibleEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTopBar(
    bibleAbbr: String,
    bookName: String,
    chapterRef: String,
    savedBibles: List<BibleEntity>,
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