package com.biblelib.feature.bibles.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.database.model.BibleEntity
import kotlin.collections.forEach

@Composable
fun MultiBibleToggleCard(
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Multi-Bible Reader", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Read parallel translations alongside your primary text",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Switch(checked = enabled, onCheckedChange = onCheckedChange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryBiblesCard(
    bibles: List<BibleEntity>,
    secondaryBibles: List<String>,
    onMove: (String, Int) -> Unit,
    onToggle: (String) -> Unit,
) {
    var reordering by remember { mutableStateOf(false) }
    val showReorderControls = reordering && secondaryBibles.size > 1

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    if (secondaryBibles.size == 1) "1 secondary bible" else "${secondaryBibles.size} secondary bibles",
                    style = MaterialTheme.typography.labelMedium,
                )
                if (secondaryBibles.size > 1) {
                    TextButton(onClick = { reordering = !reordering }) {
                        Icon(Icons.Default.SwapVert, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (reordering) "Done" else "Reorder")
                    }
                }
            }

            secondaryBibles.forEachIndexed { index, abbr ->
                val bible = bibles.find { it.abbreviation == abbr }
                ListItem(
                    leadingContent = if (showReorderControls) {
                        {
                            Column {
                                IconButton(
                                    onClick = { onMove(abbr, -1) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(24.dp),
                                ) { Icon(Icons.Default.ArrowUpward, "Move up") }
                                IconButton(
                                    onClick = { onMove(abbr, 1) },
                                    enabled = index < secondaryBibles.size - 1,
                                    modifier = Modifier.size(24.dp),
                                ) { Icon(Icons.Default.ArrowDownward, "Move down") }
                            }
                        }
                    } else null,
                    headlineContent = { Text(bible?.name ?: abbr.uppercase()) },
                    supportingContent = { Text(abbr.uppercase()) },
                    trailingContent = {
                        Checkbox(
                            checked = true,
                            onCheckedChange = { onToggle(abbr) },
                        )
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSecondaryBiblesCard(
    bibles: List<BibleEntity>,
    secondaryCount: Int,
    onToggle: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            bibles.forEach { bible ->
                ListItem(
                    headlineContent = { Text(bible.name) },
                    supportingContent = { Text(bible.abbreviation.uppercase()) },
                    trailingContent = {
                        Checkbox(
                            checked = false,
                            enabled = secondaryCount < PrefsRepo.MAX_SECONDARY_BIBLES,
                            onCheckedChange = { onToggle(bible.abbreviation) },
                        )
                    },
                )
            }
        }
    }
}
