package com.biblelib.feature.selection.view.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
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
import com.biblelib.core.designsystem.theme.ThemeSelectorDialog
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.core.ui.components.indicators.BibleCardShimmer
import com.biblelib.core.ui.components.indicators.ErrorState
import com.biblelib.feature.selection.viewmodel.SelectionViewModel
import com.biblelib.feature.selection.view.components.BibleListItem
import com.biblelib.feature.selection.view.components.ProceedBar
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionScreen(
    navController: NavController,
    viewModel: SelectionViewModel,
    themeRepo: ThemeRepo,
) {

    val uiState by viewModel.uiState.collectAsState()
    val bibles by viewModel.bibles.collectAsState()
    val selectedCount by viewModel.selectedCount.collectAsState()
    val canProceed by viewModel.canProceed.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadStep by viewModel.downloadStep.collectAsState()

    var showThemeDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchBibles()
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Saved) {
            navController.navigate(Routes.reader()) {
                popUpTo(Routes.SELECTION) {
                    inclusive = true
                }
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectorDialog(
            current = themeRepo.selectedTheme,
            onDismiss = {
                showThemeDialog = false
            },
            onThemeSelected = {
                themeRepo.setTheme(it)
                showThemeDialog = false
            }
        )
    }

    Scaffold(

        topBar = {

            AppTopBar(

                title = "Choose Your Bibles",
                tagline = "Select 1 – ${SelectionViewModel.MAX_SELECTIONS} Bibles",

                actions = {

                    if (uiState !is UiState.Loading &&
                        uiState !is UiState.Saving
                    ) {
                        IconButton(
                            onClick = viewModel::fetchBibles
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                null
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            showThemeDialog = true
                        }
                    ) {
                        Icon(
                            Icons.Default.Brightness6,
                            null
                        )
                    }
                }
            )
        },

        bottomBar = {

            ProceedBar(
                selectedCount = selectedCount,
                maxSelections = SelectionViewModel.MAX_SELECTIONS,
                canProceed = canProceed,
                isSaving = uiState is UiState.Saving,
                onProceed = viewModel::saveSelectionAndDownload
            )
        }

    ) { padding ->

        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            when (uiState) {

                UiState.Loading -> BibleCardShimmer()

                is UiState.Error -> ErrorState(
                    message = (uiState as UiState.Error).message,
                    onRetry = viewModel::fetchBibles
                )

                UiState.Saving -> {

                    val animatedProgress by animateFloatAsState(
                        targetValue = downloadProgress,
                        label = "download_progress",
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(96.dp),
                                strokeWidth = 6.dp,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text = "Downloading your primary Bible",
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = downloadStep,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )

                        Spacer(Modifier.height(24.dp))

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "The rest of your Bibles will keep downloading in the background once you reach the reader.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }

                else -> {

                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {

                        itemsIndexed(
                            bibles,
                            key = { _, it ->
                                it.data.abbreviation
                            }
                        ) { index, item ->

                            var visible by remember {
                                mutableStateOf(false)
                            }

                            LaunchedEffect(Unit) {
                                delay(index * 40L)
                                visible = true
                            }

                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn() +
                                        slideInVertically {
                                            it / 3
                                        }
                            ) {

                                BibleListItem(

                                    name = item.data.name,
                                    description = item.data.description,
                                    abbreviation = item.data.abbreviation,
                                    language = item.data.language.name,

                                    isSelected = item.isSelected,

                                    isDisabled =
                                        !item.isSelected &&
                                                selectedCount >= SelectionViewModel.MAX_SELECTIONS,

                                    onClick = {
                                        viewModel.toggleSelection(
                                            item.data.abbreviation
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
