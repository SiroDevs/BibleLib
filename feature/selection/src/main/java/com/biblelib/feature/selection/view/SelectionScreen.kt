package com.biblelib.feature.selection.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.entity.UiState
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.ui.components.indicators.BibleCardShimmer
import com.biblelib.core.ui.components.indicators.ErrorState
import com.biblelib.feature.selection.SelectionViewModel
import com.biblelib.feature.selection.view.components.BibleListItem
import com.biblelib.feature.selection.view.components.ProceedBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionScreen(
    navController: NavController,
    viewModel: SelectionViewModel,
    themeRepo: ThemeRepo,
) {
    val uiState by viewModel.uiState.collectAsState()
    val bibles by viewModel.bibles.collectAsState()

    LaunchedEffect(Unit) { viewModel.fetchBibles() }

    // Navigate to reader when saving is done
    LaunchedEffect(uiState) {
        if (uiState is UiState.Saved) {
            navController.navigate(Routes.reader()) {
                popUpTo(Routes.SELECTION) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Choose Your Bibles", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Select 1–3 versions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                )
            )
        },
        bottomBar = {
            ProceedBar(
                selectedCount = viewModel.selectedCount,
                maxSelections = viewModel.maxSelections,
                canProceed = viewModel.canProceed(),
                isSaving = uiState is UiState.Saving,
                onProceed = viewModel::saveSelectionAndDownload,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                is UiState.Loading -> BibleCardShimmer()

                is UiState.Error -> ErrorState(
                    message = (uiState as UiState.Error).message,
                    onRetry = viewModel::fetchBibles
                )

                is UiState.Saving -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Downloading primary Bible…", style = MaterialTheme.typography.bodyMedium)
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    itemsIndexed(bibles, key = { _, b -> b.data.abbreviation }) { index, item ->
                        val isSelected = item.isSelected
                        val isDisabled =
                            !isSelected && viewModel.selectedCount >= viewModel.maxSelections

                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(index * 40L)
                            visible = true
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                        ) {
                            BibleListItem(
                                name = item.data.name,
                                description = item.data.description,
                                abbreviation = item.data.abbreviation,
                                language = item.data.language.name,
                                isSelected = isSelected,
                                isDisabled = isDisabled,
                                onClick = { viewModel.toggleSelection(item.data.abbreviation) }
                            )
                        }
                    }
                }
            }
        }
    }
}
