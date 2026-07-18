package com.biblelib.feature.reader.main.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Lets the user pick a highlight color for the currently-selected verses. */
@Composable
fun HighlightColorPickerDialog(
    colors: List<String>,
    onColorChosen: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose a highlight color") },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                colors.forEach { hex ->
                    val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }
                        .getOrDefault(Color.Gray)
                    Column(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { onColorChosen(hex) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {}
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/** Once a color has been chosen, ask whether to just bookmark, or bookmark + add a note. */
@Composable
fun BookmarkOptionsDialog(
    onBookmarkOnly: () -> Unit,
    onBookmarkWithNotes: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Highlight applied") },
        text = { Text("Would you like to just bookmark these verses, or also add a note?") },
        confirmButton = {
            TextButton(onClick = onBookmarkWithNotes) { Text("Bookmark with notes") }
        },
        dismissButton = {
            TextButton(onClick = onBookmarkOnly) { Text("Bookmark only") }
        },
    )
}
