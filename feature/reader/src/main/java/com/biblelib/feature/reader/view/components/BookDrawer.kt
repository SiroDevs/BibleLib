package com.biblelib.feature.reader.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.model.BookEntity
import kotlin.text.ifEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDrawer(
    books: List<BookEntity>,
    activeBookId: String,
    onSelect: (BookEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Books",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
            items(books, key = { it.id }) { book ->
                ListItem(
                    headlineContent = { Text(book.nameLong.ifEmpty { book.name }) },
                    supportingContent = { Text(book.name) },
                    leadingContent = {
                        Text(
                            book.id.take(3),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    trailingContent = if (book.id == activeBookId) ({
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }) else null,
                    modifier = Modifier.clickable { onSelect(book) },
                )
                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}
