package com.biblelib.feature.bibles.view.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.ui.MainViewModel
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.core.ui.components.general.ConfirmDialog
import com.biblelib.feature.bibles.view.components.AddSecondaryBiblesCard
import com.biblelib.feature.bibles.view.components.MultiBibleToggleCard
import com.biblelib.feature.bibles.view.components.PrimaryBiblePickerDialog
import com.biblelib.feature.bibles.view.components.SecondaryBiblesCard
import com.biblelib.feature.bibles.view.components.SwipeableBibleRow
import com.biblelib.feature.bibles.viewmodel.BiblesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiblesScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    viewModel: BiblesViewModel,
) {
    val state by viewModel.uiState.collectAsState()
    val primary = state.bibles.find { it.abbreviation == state.primaryAbbr }
    val others = state.bibles.filter { it.abbreviation != state.primaryAbbr }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Manage Bibles",
                showGoBack = true,
                onNavIconClick = { navController.popBackStack() },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Change Selection") },
                icon = { Icon(Icons.Default.SwapHoriz, null) },
                onClick = { viewModel.requestReselection(mainViewModel) },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Primary Bible",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            item {
                if (primary != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openPrimaryPicker() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    primary.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "${primary.abbreviation.uppercase()} • PRIMARY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            TextButton(onClick = { viewModel.openPrimaryPicker() }) {
                                Text("Change")
                            }
                        }
                    }
                } else {
                    Text("No primary Bible set yet.")
                }
            }

            item { Spacer(Modifier.height(4.dp)) }
            item {
                MultiBibleToggleCard(
                    enabled = state.multiBibleEnabled,
                    onCheckedChange = viewModel::setMultiBibleEnabled,
                )
            }

            if (state.multiBibleEnabled) {
                item {
                    SecondaryBiblesCard(
                        bibles = state.bibles,
                        secondaryBibles = state.secondaryBibles,
                        onMove = viewModel::moveSecondaryBible,
                        onToggle = viewModel::toggleSecondaryBible,
                    )
                }

                val addable = state.bibles.filter {
                    it.isDownloaded && it.abbreviation != state.primaryAbbr && it.abbreviation !in state.secondaryBibles
                }
                if (addable.isNotEmpty()) {
                    item {
                        Text(
                            "Other Bibles",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                    item {
                        AddSecondaryBiblesCard(
                            bibles = addable,
                            secondaryCount = state.secondaryBibles.size,
                            onToggle = viewModel::toggleSecondaryBible,
                        )
                    }
                }
            }

            if (others.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Other Bibles",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                items(others, key = { it.abbreviation }) { bible ->
                    SwipeableBibleRow(
                        bible = bible,
                        progress = state.downloadProgress[bible.abbreviation],
                        onDelete = { viewModel.requestDelete(bible) },
                    )
                }
            }

            item { Spacer(Modifier.height(72.dp)) } // clear the FAB
        }
    }

    if (state.showPrimaryPicker) {
        PrimaryBiblePickerDialog(
            bibles = state.bibles,
            current = state.primaryAbbr,
            onDismiss = viewModel::dismissPrimaryPicker,
            onSelect = viewModel::setPrimaryBible,
        )
    }

    state.pendingDelete?.let { bible ->
        ConfirmDialog(
            title = "Remove ${bible.name}?",
            message = "This deletes all downloaded content for this Bible from your device. This can't be undone.",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete,
        )
    }
}
