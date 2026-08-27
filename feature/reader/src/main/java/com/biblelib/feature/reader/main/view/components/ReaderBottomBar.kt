package com.biblelib.feature.reader.main.view.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.feature.reader.main.viewmodel.ReaderViewModel

@Composable
fun ReaderBottomBar(
    navController: NavController,
    viewModel: ReaderViewModel,
    hasPrev: Boolean,
    hasNext: Boolean,
    chapterRef: String,
    onChapterList: () -> Unit,
    onQuickSettings: () -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.onPrimary, tonalElevation = 4.dp) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.SCRIPTURE_LISTS) },
            icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, "Scriptures") },
            label = { Text("Scriptures") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { viewModel.navigateChapter(-1) },
            enabled = hasPrev,
            icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous") },
            label = { Text("Prev") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onChapterList,
            icon = { Icon(Icons.Default.MenuBook, "Chapters") },
            label = { Text("Chapter $chapterRef", maxLines = 1, overflow = TextOverflow.Ellipsis) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { viewModel.navigateChapter(1) },
            enabled = hasNext,
            icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next") },
            label = { Text("Next") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onQuickSettings,
            icon = { Icon(Icons.Default.Tune, "Quick Settings") },
            label = { Text("Options") }
        )
    }
}
