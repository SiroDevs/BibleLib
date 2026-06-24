package com.biblelib.feature.search.view

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
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.core.database.model.SearchEntity
import com.biblelib.feature.search.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel,
) {
    val query         by viewModel.query.collectAsState()
    val results       by viewModel.results.collectAsState()
    val isSearching   by viewModel.isSearching.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                title = {
                    SearchField(
                        query     = query,
                        onChange  = viewModel::onQueryChange,
                        onClear   = viewModel::clearQuery,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                isSearching -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                query.length >= 3 && results.isEmpty() && !isSearching -> Box(
                    Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text("No results for "$query"", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                query.length >= 3 -> LazyColumn {
                    item {
                        Text(
                            "${results.size} results",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    items(results, key = { it.verseId }) { verse ->
                        SearchResultItem(verse = verse, query = query)
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
                else -> HistorySection(
                    history = searchHistory,
                    onSelect = viewModel::searchFromHistory,
                    onClear  = viewModel::clearSearchHistory,
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    TextField(
        value = query,
        onValueChange = onChange,
        placeholder = { Text("Search scriptures…") },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            focusedContainerColor   = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            focusedIndicatorColor   = androidx.compose.ui.graphics.Color.Transparent,
        ),
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, "Clear", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SearchResultItem(verse: VerseDisplay, query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = "${verse.bookId} ${verse.chapterId.substringAfter(".")}:${verse.number}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(2.dp))
        // Highlight the matching query text
        val annotated = buildAnnotatedString {
            val lower = verse.text.lowercase()
            val qLow  = query.lowercase()
            var start = 0
            var idx   = lower.indexOf(qLow)
            while (idx >= 0) {
                append(verse.text.substring(start, idx))
                withStyle(SpanStyle(
                    fontWeight = FontWeight.Bold,
                    background = MaterialTheme.colorScheme.secondaryContainer,
                )) {
                    append(verse.text.substring(idx, idx + query.length))
                }
                start = idx + query.length
                idx   = lower.indexOf(qLow, start)
            }
            append(verse.text.substring(start))
        }
        Text(
            text = annotated,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HistorySection(
    history: List<SearchEntity>,
    onSelect: (String) -> Unit,
    onClear: () -> Unit,
) {
    LazyColumn {
        if (history.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Text("Search the scriptures…",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }
        } else {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent searches", style = MaterialTheme.typography.labelMedium)
                    TextButton(onClick = onClear) { Text("Clear") }
                }
            }
            items(history, key = { it.id }) { item ->
                ListItem(
                    headlineContent = { Text(item.query) },
                    leadingContent  = { Icon(Icons.Default.History, null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                    modifier = Modifier.clickable { onSelect(item.query) }
                )
            }
        }
    }
}
