package com.biblelib.feature.selection.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.biblelib.core.database.model.BookEntity
import com.biblelib.core.ui.components.general.*
import com.biblelib.feature.selection.SelectionViewModel

@Composable
fun Step1Fab(
    viewModel: SelectionViewModel,
    onSaveConfirmed: (List<BookEntity>) -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showNoSelectionDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        ConfirmDialog(
            title = "Save Selection",
            message = "Are you sure you want to save the selected books?",
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                onSaveConfirmed(viewModel.getSelectedBookList())
                showConfirmDialog = false
            }
        )
    }

    if (showNoSelectionDialog) {
        InfoDialog(
            title = "No Selection",
            message = "Please select at least one book before saving.",
            onDismiss = { showNoSelectionDialog = false }
        )
    }

    ExtendedFloatingActionButton(
        onClick = {
            if (viewModel.getSelectedBookList().isNotEmpty()) {
                showConfirmDialog = true
            } else {
                showNoSelectionDialog = true
            }
        },
        icon = { Icon(Icons.Filled.Check, "Proceed") },
        text = { Text(text = "Proceed") },
    )
}
