package com.biblelib.feature.history.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.database.model.HistoryEntity
import com.biblelib.feature.history.viewmodel.HistoryGroup
import com.biblelib.feature.history.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalLocale

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
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (activeTab == 0) viewModel.clearReadingHistory()
                        else viewModel.clearSearchHistory()
                    }) {
                        Icon(Icons.Default.DeleteSweep, "Clear",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                )
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
