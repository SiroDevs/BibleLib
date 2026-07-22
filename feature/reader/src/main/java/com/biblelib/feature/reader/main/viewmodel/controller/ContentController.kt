package com.biblelib.feature.reader.main.viewmodel.controller

import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.core.data.repos.AnnotationRepo
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ScriptureQueueRepo
import com.biblelib.core.data.repos.TrackingRepo
import com.biblelib.core.database.model.BookEntity
import com.biblelib.core.database.model.ChapterEntity
import com.biblelib.core.database.model.HistoryEntity
import com.biblelib.feature.reader.main.utils.ReaderUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContentController(
    private val bibleRepo: BibleRepo,
    private val prefsRepo: PrefsRepo,
    private val annotationRepo: AnnotationRepo,
    private val trackingRepo: TrackingRepo,
    private val scriptureQueueRepo: ScriptureQueueRepo,
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<ReaderUiState>,
) {
    private var isFirstLoad = false

    /** Marks the next [loadVerses] call as the initial one, so it restores the last-read verse. */
    fun markFirstLoad() {
        isFirstLoad = true
    }

    suspend fun loadBooks(abbr: String, bookId: String, chapterId: String) {
        val books = bibleRepo.getLocalBooks(abbr)
        if (books.isEmpty()) {
            state.update {
                it.copy(
                    isLoading = false,
                    error = "Bible data not available. Please wait for the download to complete."
                )
            }
            return
        }

        val targetBook = books.find { it.id == bookId } ?: books.first()
        state.update { it.copy(books = books, activeBook = targetBook) }

        loadChapters(abbr, targetBook, chapterId)
    }

    private suspend fun loadChapters(abbr: String, book: BookEntity, chapterId: String) {
        val chapters = bibleRepo.getLocalChapters(abbr, book.id)
        if (chapters.isEmpty()) {
            state.update {
                it.copy(
                    isLoading = false,
                    error = "No chapters found for ${book.name}"
                )
            }
            return
        }

        val targetChapter = chapters.find { it.id == chapterId } ?: chapters.first()
        state.update { it.copy(chapters = chapters, activeChapter = targetChapter) }

        loadVerses(abbr, targetChapter)
    }

    suspend fun loadVerses(abbr: String, chapter: ChapterEntity) {
        state.update { it.copy(isLoading = true, error = null) }
        val verses = bibleRepo.getLocalVerses(abbr, chapter.id)
        if (verses == null) {
            state.update {
                it.copy(
                    isLoading = false,
                    error = "Verses not cached. Please ensure download is complete."
                )
            }
            return
        }

        val parallelMap = mutableMapOf<String, List<VerseDisplay>>()
        val multiBibleEnabled = prefsRepo.multiBibleReaderEnabled
        if (multiBibleEnabled) {
            val downloadedAbbrs = state.value.savedBibles
                .filter { it.isDownloaded }
                .map { it.abbreviation }
                .toSet()

            val orderedSecondary = prefsRepo.getSecondaryBibleList()
                .filter { it != abbr && it in downloadedAbbrs }
                .ifEmpty {
                    // Fallback for users who haven't configured a stack yet.
                    state.value.savedBibles
                        .filter { it.abbreviation != abbr && it.isDownloaded }
                        .map { it.abbreviation }
                }

            orderedSecondary.forEach { sAbbr ->
                val pVerses = bibleRepo.getLocalVerses(sAbbr, chapter.id)
                if (pVerses != null) parallelMap[sAbbr] = pVerses
            }
        }

        val bookmarks = annotationRepo.getBookmarksForChapter(abbr, chapter.id)
        val notedVerseIds = annotationRepo.getNotedVerseIds(abbr, chapter.id)

        val restoreTarget = if (isFirstLoad) prefsRepo.lastVerseId.takeIf { it.isNotEmpty() } else null
        isFirstLoad = false

        state.update {
            it.copy(
                isLoading = false,
                verses = verses,
                parallelVerses = parallelMap,
                activeChapter = chapter,
                activeBibleAbbr = abbr,
                bookmarks = bookmarks,
                notedVerseIds = notedVerseIds,
                selectedVerseIds = emptySet(),
                showColorPicker = false,
                pendingHighlightColor = null,
                multiBibleReaderEnabled = multiBibleEnabled,
                restoreVerseId = restoreTarget,
            )
        }

        prefsRepo.lastBibleAbbr = abbr
        prefsRepo.lastBookId = chapter.bookId
        prefsRepo.lastChapterId = chapter.id

        scriptureQueueRepo.syncActiveByChapter(abbr, chapter.id)

        val book = state.value.activeBook
        if (book != null) {
            trackingRepo.recordReading(
                HistoryEntity(
                    bibleAbbr = abbr,
                    bibleName = state.value.activeBible,
                    bookId = book.id,
                    bookName = book.name,
                    chapterId = chapter.id,
                    chapterRef = chapter.reference,
                    verseNumber = verses.firstOrNull()?.number ?: 1,
                )
            )
        }
    }

    fun onVerseScrollPositionChanged(verseId: String, verseNumber: Int) {
        val chapter = state.value.activeChapter ?: return
        val book = state.value.activeBook ?: return
        val abbr = state.value.activeBibleAbbr

        prefsRepo.lastVerseId = verseId

        scope.launch {
            trackingRepo.recordReading(
                HistoryEntity(
                    bibleAbbr = abbr,
                    bibleName = state.value.activeBible,
                    bookId = book.id,
                    bookName = book.name,
                    chapterId = chapter.id,
                    chapterRef = chapter.reference,
                    verseNumber = verseNumber,
                )
            )
        }
    }

    fun consumeRestoreVerseTarget() {
        state.update { it.copy(restoreVerseId = null) }
    }

    fun navigateChapter(direction: Int) {
        val chapters = state.value.chapters
        val current = state.value.activeChapter ?: return
        val idx = chapters.indexOfFirst { it.id == current.id }
        val next = chapters.getOrNull(idx + direction) ?: return
        selectChapter(next)
    }

    fun selectBible(abbr: String) {
        val chapter = state.value.activeChapter ?: return
        val newName = state.value.savedBibles.find { it.abbreviation == abbr }?.name
            ?: state.value.activeBible

        prefsRepo.primaryBible = abbr
        prefsRepo.lastBible = newName
        prefsRepo.setSecondaryBibleList(prefsRepo.getSecondaryBibleList() - abbr)

        state.update { it.copy(activeBible = newName, activeBibleAbbr = abbr) }

        scope.launch {
            loadVerses(abbr, chapter)
            loadBooks(abbr, state.value.activeBook?.id ?: "", chapter.id)
        }
    }

    fun selectBook(book: BookEntity) {
        state.update { it.copy(activeBook = book, chapters = emptyList(), verses = emptyList()) }
        scope.launch {
            loadChapters(state.value.activeBibleAbbr, book, "")
        }
    }

    fun selectChapter(chapter: ChapterEntity) {
        scope.launch {
            loadVerses(state.value.activeBibleAbbr, chapter)
        }
    }

    fun setMultiBibleReaderEnabled(enabled: Boolean) {
        prefsRepo.multiBibleReaderEnabled = enabled
        state.update { it.copy(multiBibleReaderEnabled = enabled) }
        val chapter = state.value.activeChapter ?: return
        scope.launch { loadVerses(state.value.activeBibleAbbr, chapter) }
    }
}