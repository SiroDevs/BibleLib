package com.biblelib.feature.reader.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblelib.core.common.entity.VerseDisplay

@Composable
fun VerseList(
    verses: List<VerseDisplay>,
    parallelVerses: Map<String, List<VerseDisplay>>,
    fontSizeSp: Float,
) {
    val hasParallel = parallelVerses.isNotEmpty()
    LazyColumn(
        contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(verses, key = { it.verseId }) { verse ->
            VerseRow(
                verse          = verse,
                fontSizeSp     = fontSizeSp,
                parallelTexts  = if (hasParallel) {
                    parallelVerses.mapValues { (_, pVerses) ->
                        pVerses.find { it.number == verse.number }?.text ?: ""
                    }
                } else emptyMap(),
            )
        }
    }
}

@Composable
private fun VerseRow(
    verse: VerseDisplay,
    fontSizeSp: Float,
    parallelTexts: Map<String, String>,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        // Primary verse
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = (fontSizeSp * 0.72f).sp,
                    color = MaterialTheme.colorScheme.primary,
                )) { append("${verse.number} ") }
                append(verse.text)
            },
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * 1.6f).sp,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // Parallel verses
        parallelTexts.forEach { (abbr, text) ->
            if (text.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = (fontSizeSp * 0.65f).sp,
                            color = MaterialTheme.colorScheme.secondary,
                        )) { append("[${abbr.uppercase()}] ") }
                        append(text)
                    },
                    fontSize = (fontSizeSp * 0.85f).sp,
                    lineHeight = (fontSizeSp * 1.5f).sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                )
            }
        }
    }
}