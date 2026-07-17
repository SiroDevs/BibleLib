package com.biblelib.feature.settings.view.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.AppFonts
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ThemeMode
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.core.ui.components.general.ConfirmDialog
import com.biblelib.feature.settings.view.components.SettingsSection
import com.biblelib.feature.settings.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settViewModel: SettingsViewModel,
    themeRepo: ThemeRepo,
) {
    val savedBibles by settViewModel.savedBibles.collectAsState()
    val fontSizeSp by settViewModel.fontSizeSp.collectAsState()
    val multiBibleEnabled by settViewModel.multiBibleEnabled.collectAsState()
    val secondaryBibles by settViewModel.secondaryBibles.collectAsState()
    val showClearAnnotationsDialog by settViewModel.showClearAnnotationsDialog.collectAsState()
    val currentTheme = themeRepo.selectedTheme

    val primaryAbbr = savedBibles.firstOrNull { it.abbreviation !in secondaryBibles }?.abbreviation
    val secondaryEligible = savedBibles.filter { it.isDownloaded }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings",
                showGoBack = true,
                onNavIconClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsSection(title = "Appearance") {
                    Text(
                        "Theme", style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = currentTheme == mode,
                                onClick = { themeRepo.setTheme(mode) },
                                label = {
                                    Text(
                                        mode.name.lowercase().replaceFirstChar { it.uppercase() })
                                }
                            )
                        }
                    }
                }
            }

            item {
                SettingsSection(title = "Reading") {
                    Text(
                        "Font size: ${fontSizeSp.toInt()}sp",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Slider(
                        value = fontSizeSp,
                        onValueChange = settViewModel::setFontSize,
                        valueRange = AppFonts.MIN_FONT_SP..AppFonts.MAX_FONT_SP,
                        steps = ((AppFonts.MAX_FONT_SP - AppFonts.MIN_FONT_SP) / 2).toInt() - 1,
                    )
                    Text(
                        "In the beginning God created the heavens and the earth.",
                        fontSize = TextUnit(
                            fontSizeSp,
                            TextUnitType.Sp
                        ),
                        lineHeight = TextUnit(
                            fontSizeSp * 1.6f,
                            TextUnitType.Sp
                        ),
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            }

            item {
                SettingsSection(title = "Multi-Bible Reader") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Show secondary Bibles alongside your primary text")
                        Switch(
                            checked = multiBibleEnabled,
                            onCheckedChange = settViewModel::setMultiBibleEnabled,
                        )
                    }

                    if (multiBibleEnabled) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Stack order (${secondaryBibles.size}/${PrefsRepo.MAX_SECONDARY_BIBLES})",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        secondaryBibles.forEachIndexed { index, abbr ->
                            val bible = savedBibles.find { it.abbreviation == abbr }
                            ListItem(
                                headlineContent = { Text(bible?.name ?: abbr.uppercase()) },
                                supportingContent = { Text(abbr.uppercase()) },
                                trailingContent = {
                                    Row {
                                        IconButton(
                                            onClick = { settViewModel.moveSecondaryBible(abbr, -1) },
                                            enabled = index > 0,
                                        ) { Icon(Icons.Default.ArrowUpward, "Move up") }
                                        IconButton(
                                            onClick = { settViewModel.moveSecondaryBible(abbr, 1) },
                                            enabled = index < secondaryBibles.size - 1,
                                        ) { Icon(Icons.Default.ArrowDownward, "Move down") }
                                        Checkbox(
                                            checked = true,
                                            onCheckedChange = { settViewModel.toggleSecondaryBible(abbr) },
                                        )
                                    }
                                }
                            )
                        }

                        val available = secondaryEligible.filter {
                            it.abbreviation != primaryAbbr && it.abbreviation !in secondaryBibles
                        }
                        if (available.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Add more (up to ${PrefsRepo.MAX_SECONDARY_BIBLES})",
                                style = MaterialTheme.typography.labelMedium,
                            )
                            available.forEach { bible ->
                                ListItem(
                                    headlineContent = { Text(bible.name) },
                                    supportingContent = { Text(bible.abbreviation.uppercase()) },
                                    trailingContent = {
                                        Checkbox(
                                            checked = false,
                                            enabled = secondaryBibles.size < PrefsRepo.MAX_SECONDARY_BIBLES,
                                            onCheckedChange = { settViewModel.toggleSecondaryBible(bible.abbreviation) },
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                SettingsSection(title = "Danger Zone") {
                    Text(
                        "Permanently remove every bookmark and note you've saved, across all Bibles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    OutlinedButton(
                        onClick = settViewModel::requestClearAnnotations,
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Icon(Icons.Default.DeleteSweep, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text("Clear All Bookmarks & Notes")
                    }
                }
            }
        }
    }

    if (showClearAnnotationsDialog) {
        ConfirmDialog(
            title = "Clear all bookmarks & notes?",
            message = "This permanently deletes every bookmark and note across all your Bibles. This can't be undone.",
            onConfirm = settViewModel::confirmClearAnnotations,
            onDismiss = settViewModel::dismissClearAnnotations,
        )
    }
}
