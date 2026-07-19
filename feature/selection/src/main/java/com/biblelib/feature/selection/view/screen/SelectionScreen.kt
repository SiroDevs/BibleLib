package com.biblelib.feature.selection.view.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.entity.Selectable
import com.biblelib.core.common.entity.UiState
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.designsystem.theme.ThemeSelectorDialog
import com.biblelib.core.network.dtos.BibleInfoDto
import com.biblelib.core.network.dtos.primaryCountryName
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.core.ui.components.indicators.BibleCardShimmer
import com.biblelib.core.ui.components.indicators.ErrorState
import com.biblelib.feature.selection.viewmodel.SelectionViewModel
import com.biblelib.feature.selection.view.components.BibleListItem
import com.biblelib.feature.selection.view.components.BibleSavingProgress
import com.biblelib.feature.selection.view.components.CountryGroupHeader
import com.biblelib.feature.selection.view.components.DownloadFailedState
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
    val selectedCount by viewModel.selectedCount.collectAsState()
    val canProceed by viewModel.canProceed.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadStep by viewModel.downloadStep.collectAsState()

    var showThemeDialog by remember {
        mutableStateOf(false)
    }

    val expandedGroups = remember { mutableStateMapOf<String, Boolean>() }

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

    val showChrome = uiState !is UiState.Saving && uiState !is UiState.SaveFailed

    Scaffold(
        topBar = {
            if (!showChrome) {
                AppTopBar(title = "BibleLib")
            } else {
                AppTopBar(
                    title = "BibleLib",
                    tagline = "$selectedCount / ${SelectionViewModel.MAX_SELECTIONS} bibles selected",
                    actions = {
                        IconButton(
                            onClick = viewModel::fetchBibles
                        ) {
                            Icon(Icons.Default.Refresh, null)
                        }

                        IconButton(
                            onClick = { showThemeDialog = true }
                        ) {
                            Icon(Icons.Default.Brightness6, null)
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showChrome && uiState !is UiState.Loading) {
                ProceedBar(
                    canProceed = canProceed,
                    onProceed = viewModel::saveSelectionAndDownload
                )
            }
        }
    ) { padding ->

        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            when (val state = uiState) {
                UiState.Loading -> BibleCardShimmer()

                is UiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = viewModel::fetchBibles
                )

                UiState.Saving -> {
                    val animatedProgress by animateFloatAsState(
                        targetValue = downloadProgress,
                        label = "download_progress",
                    )
                    BibleSavingProgress(progress = animatedProgress, step = downloadStep)
                }

                is UiState.SaveFailed -> {
                    DownloadFailedState(
                        message = state.message,
                        progress = state.progress,
                        onContinue = viewModel::continuePrimaryDownload,
                        onRestart = viewModel::restartPrimaryDownload,
                    )
                }

                else -> {
                    val groups = remember(bibles) { groupByCountry(bibles) }

                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        groups.forEach { (countryName, items) ->
                            val isExpanded = expandedGroups[countryName] ?: false
                            val selectedInGroup = items.count { it.isSelected }

                            item(key = "header_$countryName") {
                                CountryGroupHeader(
                                    countryName = countryName,
                                    selectedInGroup = selectedInGroup,
                                    totalInGroup = items.size,
                                    expanded = isExpanded,
                                    onClick = {
                                        expandedGroups[countryName] = !isExpanded
                                    },
                                )
                            }

                            item(key = "group_$countryName") {
                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically(),
                                    exit = shrinkVertically(),
                                ) {
                                    Column(Modifier.fillMaxWidth()) {
                                        items.forEach { item ->
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
                                                    viewModel.toggleSelection(item.data.abbreviation)
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
        }
    }
}

private fun groupByCountry(
    bibles: List<Selectable<BibleInfoDto>>
): List<Pair<String, List<Selectable<BibleInfoDto>>>> =
    bibles
        .groupBy { it.data.primaryCountryName() }
        .toSortedMap()
        .map { (country, items) -> country to items }
