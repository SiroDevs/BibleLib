package com.biblelib.feature.bibles.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.model.BibleEntity
import kotlin.collections.forEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableBibleRow(
    bible: BibleEntity,
    progress: Float?,
    isSecondary: Boolean,
    onDelete: () -> Unit,
    onToggleSecondary: (() -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    onContinueDownload: (() -> Unit)? = null,
    onRestartDownload: (() -> Unit)? = null,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> onDelete()
                SwipeToDismissBoxValue.StartToEnd -> onToggleSecondary?.invoke()
                SwipeToDismissBoxValue.Settled -> Unit
            }
            false
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = onToggleSecondary != null,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> SwipeActionBackground(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete",
                    alignment = Alignment.CenterEnd,
                )
                SwipeToDismissBoxValue.StartToEnd -> SwipeActionBackground(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    icon = if (isSecondary) Icons.Default.RemoveCircleOutline else Icons.Default.AddCircleOutline,
                    contentDescription = if (isSecondary) "Remove from secondary" else "Add to secondary",
                    alignment = Alignment.CenterStart,
                )
                SwipeToDismissBoxValue.Settled -> {}
            }
        },
    ) {
        Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
            ListItem(
                leadingContent = leadingContent,
                headlineContent = { Text(bible.name, fontWeight = FontWeight.Medium) },
                supportingContent = { Text("${bible.abbreviation.uppercase()} BIBLE") },
            )
            if (bible.downloadFailed) {
                Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
                    Text(
                        "DOWNLOAD FAILED · ${(bible.downloadProgress * 100).toInt()}% done",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(onClick = { onRestartDownload?.invoke() }) { Text("Restart") }
                        TextButton(onClick = { onContinueDownload?.invoke() }) { Text("Continue") }
                    }
                }
            } else if (!bible.isDownloaded) {
                Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
                    Text(
                        "Downloading ... ${((progress ?: bible.downloadProgress) * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    LinearProgressIndicator(
                        progress = { progress ?: bible.downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeActionBackground(
    color: Color,
    contentColor: Color,
    icon: ImageVector,
    contentDescription: String,
    alignment: Alignment,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment,
    ) {
        Icon(icon, contentDescription, tint = contentColor)
    }
}

@Composable
fun PrimaryBiblePickerDialog(
    bibles: List<BibleEntity>,
    current: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose primary Bible") },
        text = {
            Column {
                bibles.filter { it.isDownloaded }.forEach { bible ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(bible.abbreviation) },
                    ) {
                        RadioButton(
                            selected = bible.abbreviation == current,
                            onClick = { onSelect(bible.abbreviation) },
                        )
                        Text(bible.name, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}