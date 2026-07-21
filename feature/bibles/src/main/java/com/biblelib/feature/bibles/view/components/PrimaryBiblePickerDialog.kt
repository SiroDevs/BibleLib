package com.biblelib.feature.bibles.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.model.BibleEntity
import kotlin.collections.forEach

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
