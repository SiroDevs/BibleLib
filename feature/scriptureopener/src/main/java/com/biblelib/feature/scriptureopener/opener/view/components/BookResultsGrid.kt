package com.biblelib.feature.scriptureopener.opener.view.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.model.BookEntity

private const val OT_BOOK_COUNT = 39

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookResultsGrid(
    books: List<BookEntity>,
    selectedBookId: String?,
    onSelect: (BookEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ordered = remember(books) { books.sortedBy { it.sortOrder } }
    val oldTestament = remember(ordered) { ordered.take(OT_BOOK_COUNT) }
    val newTestament = remember(ordered) { ordered.drop(OT_BOOK_COUNT) }
    val otRows = remember(oldTestament) { oldTestament.chunked(2) }
    val ntRows = remember(newTestament) { newTestament.chunked(2) }
    val rowCount = maxOf(otRows.size, ntRows.size)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(10.dp),
    ) {
        LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Old Testament",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "New Testament",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                HorizontalDivider(thickness = 0.5.dp)
            }
            items(rowCount) { rowIndex ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    val otPair = otRows.getOrNull(rowIndex)
                    BookCell(otPair?.getOrNull(0), selectedBookId, onSelect, Modifier.weight(1f))
                    BookCell(otPair?.getOrNull(1), selectedBookId, onSelect, Modifier.weight(1f))
                    val ntPair = ntRows.getOrNull(rowIndex)
                    BookCell(ntPair?.getOrNull(0), selectedBookId, onSelect, Modifier.weight(1f))
                    BookCell(ntPair?.getOrNull(1), selectedBookId, onSelect, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BookCell(
    book: BookEntity?,
    selectedBookId: String?,
    onSelect: (BookEntity) -> Unit,
    modifier: Modifier,
) {
    if (book == null) {
        Box(modifier)
        return
    }
    val isSelected = book.id == selectedBookId
    Surface(
        modifier = modifier
            .padding(3.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onSelect(book) },
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(modifier = Modifier.padding(vertical = 9.dp), contentAlignment = Alignment.Center) {
            Text(
                text = book.abbreviation.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
