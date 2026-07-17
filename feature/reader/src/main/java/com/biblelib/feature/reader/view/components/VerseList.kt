package com.biblelib.feature.reader.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblelib.core.common.entity.VerseDisplay
import kotlin.math.roundToInt

private const val SWIPE_ACTION_TRIGGER_PX = 140f

@Composable
fun VerseList(
    verses: List<VerseDisplay>,
    parallelVerses: Map<String, List<VerseDisplay>>,
    fontSizeSp: Float,
    bookmarks: Map<String, String?> = emptyMap(),
    notedVerseIds: Set<String> = emptySet(),
    selectedVerseIds: Set<String> = emptySet(),
    isSelectionMode: Boolean = false,
    onLongPress: (String) -> Unit = {},
    onTap: (String) -> Unit = {},
    onSwipeBookmark: (String) -> Unit = {},
    onSwipeNotes: (String) -> Unit = {},
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
                bookmarkColor = bookmarks[verse.verseId],
                isBookmarked = bookmarks.containsKey(verse.verseId),
                hasNote = verse.verseId in notedVerseIds,
                isSelected = verse.verseId in selectedVerseIds,
                isSelectionMode = isSelectionMode,
                onLongPress = { onLongPress(verse.verseId) },
                onTap = { onTap(verse.verseId) },
                onSwipeBookmark = { onSwipeBookmark(verse.verseId) },
                onSwipeNotes = { onSwipeNotes(verse.verseId) },
            )
        }
    }
}

@Composable
private fun VerseRow(
    verse: VerseDisplay,
    fontSizeSp: Float,
    parallelTexts: Map<String, String>,
    bookmarkColor: String?,
    isBookmarked: Boolean,
    hasNote: Boolean,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onLongPress: () -> Unit,
    onTap: () -> Unit,
    onSwipeBookmark: () -> Unit,
    onSwipeNotes: () -> Unit,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }

    val rowBackground = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        bookmarkColor != null -> runCatching { Color(android.graphics.Color.parseColor(bookmarkColor)) }
            .getOrDefault(Color.Transparent).copy(alpha = 0.35f)
        else -> Color.Transparent
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Background reveal icons behind the swiping row.
        Box(modifier = Modifier.fillMaxWidth()) {
            if (offsetX > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                ) {
                    Icon(
                        Icons.Default.Bookmark,
                        contentDescription = "Bookmark",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            } else if (offsetX < 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                ) {
                    Icon(
                        Icons.Default.EditNote,
                        contentDescription = "Notes",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .clip(RoundedCornerShape(6.dp))
                .background(rowBackground)
                .pointerInput(isSelectionMode) {
                    if (!isSelectionMode) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                when {
                                    offsetX >= SWIPE_ACTION_TRIGGER_PX -> onSwipeBookmark()
                                    offsetX <= -SWIPE_ACTION_TRIGGER_PX -> onSwipeNotes()
                                }
                                offsetX = 0f
                            },
                            onDragCancel = { offsetX = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                offsetX = (offsetX + dragAmount).coerceIn(-220f, 220f)
                            }
                        )
                    }
                }
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onLongClick = onLongPress,
                    onClick = { if (isSelectionMode) onTap() },
                )
                .padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            // Primary verse
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = (fontSizeSp * 0.72f).sp,
                        color = MaterialTheme.colorScheme.primary,
                    )) { append("${verse.number} ") }
                    if (isBookmarked && bookmarkColor == null) {
                        append("\uD83D\uDD16 ") // quick-bookmark icon glyph inline
                    }
                    append(verse.text)
                    if (hasNote) {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                            append(" \uD83D\uDCDD")
                        }
                    }
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
}
