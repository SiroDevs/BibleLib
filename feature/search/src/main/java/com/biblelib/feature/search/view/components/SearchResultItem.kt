package com.biblelib.feature.search.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.biblelib.core.common.entity.VerseDisplay

@Composable
fun SearchResultItem(verse: VerseDisplay, query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = "${verse.bookId} ${verse.chapterId.substringAfter(".")}:${verse.number}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(2.dp))
        // Highlight the matching query text
        val annotated = buildAnnotatedString {
            val lower = verse.text.lowercase()
            val qLow = query.lowercase()
            var start = 0
            var idx = lower.indexOf(qLow)
            while (idx >= 0) {
                append(verse.text.substring(start, idx))
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        background = MaterialTheme.colorScheme.secondaryContainer,
                    )
                ) {
                    append(verse.text.substring(idx, idx + query.length))
                }
                start = idx + query.length
                idx = lower.indexOf(qLow, start)
            }
            append(verse.text.substring(start))
        }
        Text(
            text = annotated,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
