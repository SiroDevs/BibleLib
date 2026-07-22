package com.biblelib.feature.reader.main.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.feature.reader.main.utils.ReaderUiState
import com.biblelib.feature.reader.main.viewmodel.ReaderViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ReaderFab(
    state: ReaderUiState,
    navController: NavController,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    val isAtTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    val showScrollToTop by remember {
        derivedStateOf { !isAtTop }
    }

    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(end = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End,
    ) {
        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            SmallFloatingActionButton(
                onClick = { scope.launch { listState.animateScrollToItem(0) } },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Rudi Juu")
            }
        }

        ExtendedFloatingActionButton(
            onClick = { navController.navigate(Routes.scriptureOpener(state.activeBibleAbbr, state.activeBible)) },
            expanded = isAtTop,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            icon = { Icon(Icons.Filled.ManageSearch, "Scripture Opener") },
            text = { Text("Scripture Opener") },
        )
    }
}


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
            label = { Text(chapterRef, maxLines = 1, overflow = TextOverflow.Ellipsis) }
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
