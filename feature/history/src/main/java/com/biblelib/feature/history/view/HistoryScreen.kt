package com.biblelib.feature.history.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.database.model.HistoryEntity
import com.biblelib.feature.history.viewmodel.HistoryGroup
import com.biblelib.feature.history.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalLocale
import com.biblelib.core.ui.components.action.AppTopBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel,
) {
    val state   by viewModel.uiState.collectAsState()
    var activeTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "History",
                showGoBack = true,
                onNavIconClick = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = {
                        if (activeTab == 0) viewModel.clearReadingHistory()
                        else viewModel.clearSearchHistory()
                    }) {
                        Icon(Icons.Default.DeleteSweep, "Clear",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = activeTab) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 },
                    text = { Text("Reading") })
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 },
                    text = { Text("Searches") })
            }

            when (activeTab) {
                0 -> ReadingHistoryTab(
                    groups    = state.readingHistory,
                    isLoading = state.isLoading,
                )
                1 -> SearchHistoryTab(
                    history   = state.searchHistory,
                    isLoading = state.isLoading,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReadingHistoryTab(groups: List<HistoryGroup>, isLoading: Boolean) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (groups.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No reading history yet.", color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
        }
        return
    }

    LazyColumn {
        groups.forEach { group ->
            stickyHeader(key = group.dateLabel) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        text = group.dateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(group.entries, key = { it.id }) { entry ->
                HistoryEntryItem(entry = entry)
                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun HistoryEntryItem(entry: HistoryEntity) {
    val fmt = SimpleDateFormat("h:mm a", LocalLocale.current.platformLocale)
    ListItem(
        headlineContent = { Text("${entry.bookName} — ${entry.chapterRef}") },
        supportingContent = {
            Text(
                "${entry.bibleAbbr.uppercase()} · ${fmt.format(Date(entry.readAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        },
        leadingContent = {
            Icon(Icons.Default.MenuBook, null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        },
        modifier = Modifier.clickable {},
    )
}

@Composable
private fun SearchHistoryTab(history: List<com.biblelib.core.database.model.SearchEntity>, isLoading: Boolean) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (history.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No searches yet.", color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
        }
        return
    }
    LazyColumn {
        items(history, key = { it.id }) { search ->
            val fmt = SimpleDateFormat("MMM d, h:mm a", LocalLocale.current.platformLocale)
            ListItem(
                headlineContent = { Text(search.query) },
                supportingContent = {
                    Text(
                        fmt.format(Date(search.searchedAt)),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.Search, null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                },
                modifier = Modifier.clickable {}
            )
            HorizontalDivider(thickness = 0.5.dp)
        }
    }
}
