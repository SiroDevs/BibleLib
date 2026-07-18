package com.biblelib.feature.reader.main.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.model.BibleEntity
import kotlin.collections.forEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleSelectorSheet(
    savedBibles: List<BibleEntity>,
    activeBibleAbbr: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Switch Your Primary Bible",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        savedBibles.forEach { bible ->
            ListItem(
                headlineContent = { Text(bible.name) },
                supportingContent = { Text("${bible.abbreviation.uppercase()} BIBLE") },
                trailingContent = {
                    if (!bible.isDownloaded) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(4.dp))
                            Text("Downloading", style = MaterialTheme.typography.labelSmall)
                        }
                    } else if (bible.abbreviation == activeBibleAbbr) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                modifier = Modifier.clickable(enabled = bible.isDownloaded) {
                    onSelect(bible.abbreviation)
                },
            )
            HorizontalDivider(thickness = 0.5.dp)
        }
        Spacer(Modifier.height(32.dp))
    }
}
