package com.biblelib.feature.scriptureopener.opener.viewmodel

import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.core.database.model.BookEntity
import com.biblelib.core.database.model.ChapterEntity
import java.util.UUID

/** Which floating results panel (if any) is currently showing for a row. */
enum class ExpandedField { NONE, BOOK, CHAPTER, VERSE }

/**
 * State for a single "book / chapter / verse" search row inside the Scripture Opener.
 * A row becomes [locked] once its scripture has been added to the queue, at which point
 * it renders as a compact read-only summary and a fresh blank row appears beneath it.
 */
data class ScriptureSearchRowState(
    val key: String = UUID.randomUUID().toString(),
    val locked: Boolean = false,
    val expanded: ExpandedField = ExpandedField.BOOK,

    val books: List<BookEntity> = emptyList(),
    val selectedBook: BookEntity? = null,

    val isLoadingChapters: Boolean = false,
    val chapters: List<ChapterEntity> = emptyList(),
    val selectedChapter: ChapterEntity? = null,

    val isLoadingVerses: Boolean = false,
    val verses: List<VerseDisplay> = emptyList(),
    val selectedVerseNumber: Int? = null,
) {
    val bookLabel: String get() = selectedBook?.name ?: ""
    val chapterLabel: String get() = selectedChapter?.number ?: ""
    val verseLabel: String get() = selectedVerseNumber?.toString() ?: ""

    val canExpandChapter: Boolean get() = selectedBook != null
    val canExpandVerse: Boolean get() = selectedChapter != null

    val isComplete: Boolean get() = selectedBook != null && selectedChapter != null && selectedVerseNumber != null

    val selectedVerseId: String?
        get() = verses.firstOrNull { it.number == selectedVerseNumber }?.verseId

    val reference: String
        get() = if (selectedBook != null && selectedChapter != null && selectedVerseNumber != null) {
            "${selectedBook.name} ${selectedChapter.number}:$selectedVerseNumber"
        } else ""
}
