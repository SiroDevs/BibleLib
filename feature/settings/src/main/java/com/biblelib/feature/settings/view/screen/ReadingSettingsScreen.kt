package com.biblelib.feature.settings.view.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.AppFonts
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.designsystem.customization.AppFontFamilies
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.feature.settings.view.components.SettingsGroupLabel
import com.biblelib.feature.settings.view.components.SettingsSwitchRow
import com.biblelib.feature.settings.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingSettingsScreen(
    navController: NavController,
    settViewModel: SettingsViewModel,
) {
    val fontSizeSp by settViewModel.fontSizeSp.collectAsState()
    val fontFamilyId by settViewModel.readerFontFamilyId.collectAsState()
    val savedBibles by settViewModel.savedBibles.collectAsState()
    val multiBibleEnabled by settViewModel.multiBibleEnabled.collectAsState()
    val secondaryBibles by settViewModel.secondaryBibles.collectAsState()

    val primaryAbbr = savedBibles.firstOrNull { it.abbreviation !in secondaryBibles }?.abbreviation
    val secondaryEligible = savedBibles.filter { it.isDownloaded }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Reading",
                showGoBack = true,
                onNavIconClick = { navController.popBackStack() },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { SettingsGroupLabel("Text") }
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Font size: ${fontSizeSp.toInt()}sp",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        Slider(
                            value = fontSizeSp,
                            onValueChange = settViewModel::setFontSize,
                            valueRange = AppFonts.MIN_FONT_SP..AppFonts.MAX_FONT_SP,
                            steps = ((AppFonts.MAX_FONT_SP - AppFonts.MIN_FONT_SP) / 2).toInt() - 1,
                        )
                        val previewFamily = AppFontFamilies.byId(fontFamilyId).family
                        Text(
                            "In the beginning God created the heavens and the earth.",
                            fontFamily = previewFamily,
                            fontSize = TextUnit(fontSizeSp, TextUnitType.Sp),
                            lineHeight = TextUnit(fontSizeSp * 1.6f, TextUnitType.Sp),
                            modifier = Modifier.padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        )
                    }
                }
            }

            item {
                Text(
                    "Font (${AppFontFamilies.ALL.size} available)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 2.dp),
                )
            }
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                ) {
                    Column {
                        AppFontFamilies.ALL.forEach { font ->
                            val selected = font.id == fontFamilyId
                            ListItem(
                                headlineContent = {
                                    Text(
                                        font.displayName,
                                        fontFamily = font.family,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        "The quick brown fox jumps over the lazy dog",
                                        fontFamily = font.family,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                },
                                trailingContent = {
                                    if (selected) {
                                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                colors = if (selected) {
                                    ListItemDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    )
                                } else ListItemDefaults.colors(containerColor = Color.Transparent),
                                modifier = Modifier.clickable { settViewModel.setReaderFontFamily(font.id) },
                            )
                        }
                    }
                }
            }

            item { SettingsGroupLabel("Multi-Bible Reader") }
            item {
                SettingsSwitchRow(
                    icon = Icons.Default.TextFields,
                    title = "Show secondary Bibles",
                    subtitle = "Read parallel translations alongside your primary text",
                    checked = multiBibleEnabled,
                    onCheckedChange = settViewModel::setMultiBibleEnabled,
                )
            }

            if (multiBibleEnabled) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    ) {
                        Column(Modifier.padding(vertical = 8.dp)) {
                            Text(
                                "Stack order (${secondaryBibles.size}/${PrefsRepo.MAX_SECONDARY_BIBLES})",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
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
                                Text(
                                    "Add more",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
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
            }
        }
    }
}
