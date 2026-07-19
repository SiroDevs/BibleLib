package com.biblelib.feature.reader.main.utils

import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.core.database.model.BibleEntity
import com.biblelib.core.database.model.BookEntity
import com.biblelib.core.database.model.ChapterEntity

data class NotesNavRequest(
    val bibleAbbr: String,
    val verseId: String,
    val bookId: String,
    val chapterId: String,
    val title: String,
    val verseText: String,
)

data class ReaderUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val savedBibles: List<BibleEntity> = emptyList(),
    val activeBible: String = "",
    val activeBibleAbbr: String = "",
    val books: List<BookEntity> = emptyList(),
    val activeBook: BookEntity? = null,
    val chapters: List<ChapterEntity> = emptyList(),
    val activeChapter: ChapterEntity? = null,
    val verses: List<VerseDisplay> = emptyList(),
    val parallelVerses: Map<String, List<VerseDisplay>> = emptyMap(),
    val fontSizeSp: Float = 18f,
    val fontFamilyId: String = "default",
    val readerBackgroundId: String = "default",
    val multiBibleReaderEnabled: Boolean = true,
    val restoreVerseId: String? = null,

    val bookmarks: Map<String, String?> = emptyMap(),
    val notedVerseIds: Set<String> = emptySet(),

    val selectedVerseIds: Set<String> = emptySet(),
    val showColorPicker: Boolean = false,
    val pendingHighlightColor: String? = null,
    val notesNavRequest: NotesNavRequest? = null,

    val downloadProgress: Map<String, Float> = emptyMap(),
) {
    val isSelectionMode: Boolean get() = selectedVerseIds.isNotEmpty()
}