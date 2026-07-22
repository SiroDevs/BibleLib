package com.biblelib.feature.scriptureopener.opener.view.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Relative widths of the Book / Chapter / Verse fields in the search row (see ScriptureField
 * usages in ScriptureSearch.kt). Kept here so the pointer arrow below a results grid lines up
 * with the field it is populating.
 */
internal val FIELD_WEIGHTS = listOf(1.3f, 1f, 1f)

internal const val FIELD_INDEX_BOOK = 0
internal const val FIELD_INDEX_CHAPTER = 1
internal const val FIELD_INDEX_VERSE = 2

@Composable
fun FieldPointerArrow(fieldIndex: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        FIELD_WEIGHTS.forEachIndexed { index, weight ->
            Box(
                modifier = Modifier
                    .weight(weight),
                contentAlignment = Alignment.Center,
            ) {
                if (index == fieldIndex) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
