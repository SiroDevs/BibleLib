package com.biblelib.feature.reader.main.view.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ChapterNavBar(
    hasPrev: Boolean,
    hasNext: Boolean,
    chapterRef: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onChapterList: () -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.onPrimary, tonalElevation = 4.dp) {
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
