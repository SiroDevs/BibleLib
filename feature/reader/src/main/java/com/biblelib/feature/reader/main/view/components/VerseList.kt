package com.biblelib.feature.reader.main.view.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.feature.reader.main.utils.ReaderUiState
import com.biblelib.feature.reader.main.viewmodel.ReaderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.roundToInt

private const val SWIPE_ACTION_TRIGGER_PX = 140f

/** Keys for the two sentinel items that trigger auto chapter navigation, see [VerseList]. */
private const val PREV_CHAPTER_KEY = "__prev_chapter_sentinel__"
private const val NEXT_CHAPTER_KEY = "__next_chapter_sentinel__"

/** How long a chapter-edge sentinel must stay on screen before we actually navigate. */
private const val CHAPTER_TRANSITION_DELAY_MS = 550L

@Composable
fun VerseList(
    state: ReaderUiState,
    viewModel: ReaderViewModel,
    fontFamily: FontFamily = FontFamily.Default,
    listState: LazyListState = rememberLazyListState(),
    hasPrevChapter: Boolean = false,
    hasNextChapter: Boolean = false,
    prevChapterLabel: String = "Previous chapter",
    nextChapterLabel: String = "Next chapter",
    onNavigatePrevChapter: () -> Unit = {},
    onNavigateNextChapter: () -> Unit = {},
) {
    val hasParallel = state.parallelVerses.isNotEmpty()

    // The prev-chapter sentinel (when present) sits at LazyColumn index 0, ahead of the verses.
    val itemIndexOffset = if (hasPrevChapter) 1 else 0

    // Task: always scroll to the target verse (first verse on a normal chapter change, or the
    // exact verse requested via search) once it's part of the list.
    LaunchedEffect(state.activeChapter?.id, state.verses, state.restoreVerseId) {
        val target = state.restoreVerseId
        if (target != null && state.verses.isNotEmpty()) {
            val idx = state.verses.indexOfFirst { it.verseId == target }
            if (idx >= 0) listState.scrollToItem(idx + itemIndexOffset)
            viewModel.consumeRestoreVerseTarget()
        }
    }

    var isTransitioningPrev by remember(state.activeChapter?.id) { mutableStateOf(false) }
    var isTransitioningNext by remember(state.activeChapter?.id) { mutableStateOf(false) }

    // Task: auto-navigate to the previous chapter once its sentinel is fully revealed at the top.
    LaunchedEffect(listState, hasPrevChapter, state.activeChapter?.id) {
        if (!hasPrevChapter) {
            isTransitioningPrev = false
            return@LaunchedEffect
        }
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.firstOrNull()?.key == PREV_CHAPTER_KEY &&
                !listState.canScrollBackward
        }.distinctUntilChanged().collectLatest { revealed ->
            if (revealed) {
                isTransitioningPrev = true
                delay(CHAPTER_TRANSITION_DELAY_MS)
                onNavigatePrevChapter()
            } else {
                isTransitioningPrev = false
            }
        }
    }

    // Task: auto-navigate to the next chapter once its sentinel is fully revealed at the bottom.
    LaunchedEffect(listState, hasNextChapter, state.activeChapter?.id) {
        if (!hasNextChapter) {
            isTransitioningNext = false
            return@LaunchedEffect
        }
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.key == NEXT_CHAPTER_KEY &&
                !listState.canScrollForward
        }.distinctUntilChanged().collectLatest { revealed ->
            if (revealed) {
                isTransitioningNext = true
                delay(CHAPTER_TRANSITION_DELAY_MS)
                onNavigateNextChapter()
            } else {
                isTransitioningNext = false
            }
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (hasPrevChapter) {
            item(key = PREV_CHAPTER_KEY) {
                ChapterTransitionIndicator(
                    label = prevChapterLabel,
                    icon = Icons.Filled.KeyboardArrowUp,
                    isTransitioning = isTransitioningPrev,
                    iconAbove = true,
                )
            }
        }

        items(state.verses, key = { it.verseId }) { verse ->
            VerseRow(
                verse = verse,
                fontSizeSp = state.fontSizeSp,
                fontFamily = fontFamily,
                highlightQuery = state.highlightQuery,
                parallelTexts = if (hasParallel) {
                    state.parallelVerses.mapValues { (_, pVerses) ->
                        pVerses.find { it.number == verse.number }?.text ?: ""
                    }
                } else emptyMap(),
                bookmarkColor = state.bookmarks[verse.verseId],
                isBookmarked = state.bookmarks.containsKey(verse.verseId),
                hasNote = verse.verseId in state.notedVerseIds,
                isSelected = verse.verseId in state.selectedVerseIds,
                isSelectionMode = state.isSelectionMode,
                onLongPress = { viewModel.toggleVerseSelected(verse.verseId) },
                onTap = { viewModel.toggleVerseSelected(verse.verseId) },
                onSwipeBookmark = { viewModel.quickToggleBookmark(verse.verseId) },
                onSwipeNotes = { viewModel.requestNotesForVerse(verse.verseId) },
            )
        }

        if (hasNextChapter) {
            item(key = NEXT_CHAPTER_KEY) {
                ChapterTransitionIndicator(
                    label = nextChapterLabel,
                    icon = Icons.Filled.KeyboardArrowDown,
                    isTransitioning = isTransitioningNext,
                    iconAbove = false,
                )
            }
        }
    }
}

/**
 * Shown at the top/bottom edge of a chapter. Bounces a directional arrow to invite the user to
 * keep scrolling, then swaps to a spinner once the edge has been reached and a chapter change is
 * about to happen — giving a clear cue that the app is taking them to the prev/next chapter.
 */
@Composable
private fun ChapterTransitionIndicator(
    label: String,
    icon: ImageVector,
    isTransitioning: Boolean,
    iconAbove: Boolean,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chapter_edge_bounce")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "chapter_edge_bounce_value",
    )
    val bounceOffset = 4.dp * (if (iconAbove) -bounce else bounce)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AnimatedContent(
            targetState = isTransitioning,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "chapter_edge_content",
        ) { transitioning ->
            if (transitioning) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Opening $label…",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.offset(y = bounceOffset),
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun VerseRow(
    verse: VerseDisplay,
    fontSizeSp: Float,
    fontFamily: FontFamily,
    highlightQuery: String?,
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

    val highlightColor = MaterialTheme.colorScheme.secondaryContainer

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
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = (fontSizeSp * 0.72f).sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    ) { append("${verse.number} ") }
                    if (isBookmarked && bookmarkColor == null) {
                        append("\uD83D\uDD16 ") // quick-bookmark icon glyph inline
                    }
                    appendHighlighted(verse.text, highlightQuery, highlightColor)
                    if (hasNote) {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                            append(" \uD83D\uDCDD")
                        }
                    }
                },
                fontSize = fontSizeSp.sp,
                lineHeight = (fontSizeSp * 1.6f).sp,
                fontFamily = fontFamily,
                color = MaterialTheme.colorScheme.onBackground,
            )

            parallelTexts.forEach { (abbr, text) ->
                if (text.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (fontSizeSp * 0.65f).sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            ) { append("[${abbr.uppercase()}] ") }
                            appendHighlighted(text, highlightQuery, highlightColor)
                        },
                        fontSize = (fontSizeSp * 0.85f).sp,
                        lineHeight = (fontSizeSp * 1.5f).sp,
                        fontFamily = fontFamily,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    )
                }
            }
        }
    }
}

/** Appends [text] to this [androidx.compose.ui.text.AnnotatedString.Builder], highlighting every
 * case-insensitive occurrence of [query] (when non-blank) with [highlightColor]. */
private fun androidx.compose.ui.text.AnnotatedString.Builder.appendHighlighted(
    text: String,
    query: String?,
    highlightColor: Color,
) {
    if (query.isNullOrBlank()) {
        append(text)
        return
    }
    val lower = text.lowercase()
    val qLower = query.lowercase()
    var start = 0
    var idx = lower.indexOf(qLower)
    while (idx >= 0) {
        append(text.substring(start, idx))
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Bold,
                background = highlightColor,
            )
        ) {
            append(text.substring(idx, idx + query.length))
        }
        start = idx + query.length
        idx = lower.indexOf(qLower, start)
    }
    append(text.substring(start))
}
