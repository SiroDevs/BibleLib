package com.biblelib.feature.settings.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.AppFonts
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.ThemeMode
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.ui.MainViewModel
import com.biblelib.feature.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    settViewModel: SettingsViewModel,
    themeRepo: ThemeRepo,
) {
    val savedBibles by settViewModel.savedBibles.collectAsState()
    val fontSizeSp by settViewModel.fontSizeSp.collectAsState()
    val currentTheme = themeRepo.selectedTheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                )
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
            // ── Theme ────────────────────────────────────────────────────────
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

            // ── Font size ─────────────────────────────────────────────────────
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
                        fontSize = androidx.compose.ui.unit.TextUnit(
                            fontSizeSp,
                            androidx.compose.ui.unit.TextUnitType.Sp
                        ),
                        lineHeight = androidx.compose.ui.unit.TextUnit(
                            fontSizeSp * 1.6f,
                            androidx.compose.ui.unit.TextUnitType.Sp
                        ),
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            }

            // ── Your bibles ───────────────────────────────────────────────────
            item {
                SettingsSection(title = "Your Bibles") {
                    savedBibles.forEach { bible ->
                        ListItem(
                            headlineContent = { Text(bible.name, fontWeight = FontWeight.Medium) },
                            supportingContent = {
                                Text(
                                    if (bible.isDownloaded) "Downloaded" else "Downloading…",
                                    color = if (bible.isDownloaded) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.secondary,
                                )
                            },
                            leadingContent = {
                                Text(
                                    bible.abbreviation.uppercase().take(3),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingContent = {
                                if (savedBibles.size > 1) {
                                    IconButton(onClick = { settViewModel.removeBible(bible.abbreviation) }) {
                                        Icon(
                                            Icons.Default.Delete, "Remove",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { settViewModel.requestReselection(mainViewModel) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.SwapHoriz, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Change Bible Selection")
                    }
                }
            }

            // ── More ──────────────────────────────────────────────────────────
            item {
                SettingsSection(title = "More") {
                    ListItem(
                        headlineContent = { Text("Help & Support") },
                        leadingContent = {
                            Icon(
                                Icons.Default.HelpOutline,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                        modifier = Modifier.clickable { navController.navigate(Routes.HELP) }
                    )
                    ListItem(
                        headlineContent = { Text("Support BibleLib") },
                        leadingContent = {
                            Icon(
                                Icons.Default.Favorite,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                        modifier = Modifier.clickable { navController.navigate(Routes.DONATION) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}
