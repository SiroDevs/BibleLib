package com.biblelib.feature.search.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.entities.BibleEntity

@Composable
fun BibleFilterStrip(
    bibles: List<BibleEntity>,
    selectedAbbr: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (bibles.size <= 1) return

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(bibles, key = { it.abbreviation }) { bible ->
            val isSelected = bible.abbreviation == selectedAbbr
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(bible.abbreviation) },
                label = { Text(bible.abbreviation.uppercase()) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}
