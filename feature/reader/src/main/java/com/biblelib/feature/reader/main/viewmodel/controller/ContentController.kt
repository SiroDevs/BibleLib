package com.biblelib.feature.reader.main.viewmodel.controller

import com.biblelib.core.casting.data.CastingRepo
import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.core.data.repos.AnnotationRepo
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ScriptureQueueRepo
import com.biblelib.core.data.repos.TrackingRepo
import com.biblelib.core.database.entities.BookEntity
import com.biblelib.core.database.entities.ChapterEntity
import com.biblelib.core.database.entities.HistoryEntity
import com.biblelib.feature.reader.main.utils.ReaderUiState
import com.biblelib.feature.reader.main.utils.ScrollTarget
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
    private val castingRepo: CastingRepo,
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<ReaderUiState>,
) {
    private var isFirstLoad = false

    fun markFirstLoad() {
        isFirstLoad = true
    }

    suspend fun loadBooks(
        abbr: String,
        bookId: String,
        chapterId: String,
        scrollTarget: ScrollTarget? = null,
    ) {
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

        loadChapters(abbr, targetBook, chapterId, scrollTarget = scrollTarget)
    }

    private suspend fun loadChapters(
        abbr: String,
        book: BookEntity,
        chapterId: String,
        forceScrollToFirstVerse: Boolean = false,
        scrollTarget: ScrollTarget? = null,
    ) {
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

        loadVerses(
            abbr,
            targetChapter,
            scrollTarget = scrollTarget,
            forceScrollToFirstVerse = forceScrollToFirstVerse,
        )
    }

    suspend fun loadVerses(
        abbr: String,
        chapter: ChapterEntity,
        scrollTarget: ScrollTarget? = null,
        forceScrollToFirstVerse: Boolean = false,
    ) {
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

        val resolvedTarget: ScrollTarget? = when {
            scrollTarget != null -> scrollTarget
            forceScrollToFirstVerse -> verses.firstOrNull()?.verseId?.let { ScrollTarget(it) }
            isFirstLoad -> prefsRepo.lastVerseId.takeIf { it.isNotEmpty() }?.let { ScrollTarget(it) }
            else -> null
        }
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
                restoreVerseId = resolvedTarget?.verseId,
                highlightQuery = resolvedTarget?.highlightQuery,
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

            val secondaryNames = parallelMap.keys.mapNotNull { sAbbr ->
                state.value.savedBibles.find { it.abbreviation == sAbbr }?.name
            }
            val startIndex = resolvedTarget?.verseId
                ?.let { targetId -> verses.indexOfFirst { it.verseId == targetId } }
                ?.takeIf { it >= 0 } ?: 0

            castingRepo.publishReading(
                bibleName = state.value.activeBible,
                bookName = book.name,
                chapterRef = chapter.reference,
                verses = verses.map { it.text },
                indicators = verses.map { it.number.toString() },
                currentIndex = startIndex,
                multiBibleEnabled = multiBibleEnabled,
                secondaryBibleNames = secondaryNames,
            )
        }
    }

    fun onVerseScrollPositionChanged(verseId: String, verseNumber: Int) {
        val chapter = state.value.activeChapter ?: return
        val book = state.value.activeBook ?: return
        val abbr = state.value.activeBibleAbbr
        prefsRepo.lastVerseId = verseId

        val verseIndex = state.value.verses.indexOfFirst { it.verseId == verseId }
        if (verseIndex >= 0) castingRepo.updateIndex(verseIndex)

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
            loadChapters(state.value.activeBibleAbbr, book, "", forceScrollToFirstVerse = true)
        }
    }

    fun selectChapter(chapter: ChapterEntity, scrollTarget: ScrollTarget? = null) {
        scope.launch {
            loadVerses(
                state.value.activeBibleAbbr,
                chapter,
                scrollTarget = scrollTarget,
                forceScrollToFirstVerse = scrollTarget == null,
            )
        }
    }

    fun setMultiBibleReaderEnabled(enabled: Boolean) {
        prefsRepo.multiBibleReaderEnabled = enabled
        state.update { it.copy(multiBibleReaderEnabled = enabled) }
        val chapter = state.value.activeChapter ?: return
        scope.launch { loadVerses(state.value.activeBibleAbbr, chapter) }
    }
}