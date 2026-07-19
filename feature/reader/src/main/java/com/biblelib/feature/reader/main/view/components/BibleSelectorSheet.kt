package com.biblelib.feature.reader.main.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.model.BibleEntity
import kotlin.collections.forEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleSelectorSheet(
    savedBibles: List<BibleEntity>,
    activeBibleAbbr: String,
    downloadProgress: Map<String, Float> = emptyMap(),
    onSelect: (String) -> Unit,
    onRetryDownload: (String) -> Unit = {},
    onRestartDownload: (String) -> Unit = {},
    onOpenBibles: () -> Unit = {},
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
                supportingContent = {
                    when {
                        bible.downloadFailed -> Text(
                            "DOWNLOAD FAILED · ${(bible.downloadProgress * 100).toInt()}% done",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                        )
                        else -> Text("${bible.abbreviation.uppercase()} BIBLE")
                    }
                },
                trailingContent = {
                    when {
                        bible.downloadFailed -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = "Download failed",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            TextButton(onClick = { onRestartDownload(bible.abbreviation) }) {
                                Text("Restart", style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(onClick = { onRetryDownload(bible.abbreviation) }) {
                                Text("Continue", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        !bible.isDownloaded -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(4.dp))
                            val progress = downloadProgress[bible.abbreviation] ?: bible.downloadProgress
                            Text(
                                "Downloading ${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                        bible.abbreviation == activeBibleAbbr -> Icon(
                            Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.clickable(enabled = bible.isDownloaded) {
                    onSelect(bible.abbreviation)
                },
            )
            HorizontalDivider(thickness = 0.5.dp)
        }

        TextButton(
            onClick = onOpenBibles,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Icon(Icons.Default.LibraryBooks, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Manage Bibles")
        }

        Spacer(Modifier.height(24.dp))
    }
}
