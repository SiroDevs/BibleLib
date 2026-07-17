package com.biblelib.feature.reader.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.biblelib.core.common.entity.*
import com.biblelib.core.data.repos.AnnotationRepo
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.TrackingRepo
import com.biblelib.core.database.model.BookEntity
import com.biblelib.core.database.model.ChapterEntity
import com.biblelib.core.database.model.BibleEntity
import com.biblelib.core.database.model.HistoryEntity
import javax.inject.Inject

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
    val multiBibleReaderEnabled: Boolean = true,

    // Bookmarks/notes for the currently displayed chapter.
    val bookmarks: Map<String, String?> = emptyMap(), // verseId -> colorHex (null = quick bookmark)
    val notedVerseIds: Set<String> = emptySet(),

    // Multi-select (long-press) state.
    val selectedVerseIds: Set<String> = emptySet(),
    val showColorPicker: Boolean = false,
    val pendingHighlightColor: String? = null, // set once a color has been chosen, awaiting bookmark-only/with-notes choice

    val notesNavRequest: NotesNavRequest? = null,
) {
    val isSelectionMode: Boolean get() = selectedVerseIds.isNotEmpty()
}

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val bibleRepo: BibleRepo,
    private val trackingRepo: TrackingRepo,
    private val prefsRepo: PrefsRepo,
    private val annotationRepo: AnnotationRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun initialize(initialBible: String, initialBibleAbbr: String, initialBookId: String, initialChapterId: String) {
        viewModelScope.launch {
            try {
                val bibles = bibleRepo.getbibles()
                if (bibles.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No Bibles downloaded yet."
                        )
                    }
                    return@launch
                }

                val bibleName = initialBible.ifEmpty {
                    prefsRepo.lastBible.ifEmpty { bibles.first().name }
                }

                val bibleAbbr = initialBibleAbbr.ifEmpty {
                    prefsRepo.lastBibleAbbr.ifEmpty { bibles.first().abbreviation }
                }

                _uiState.update {
                    it.copy(
                        savedBibles = bibles,
                        activeBible = bibleName,
                        activeBibleAbbr = bibleAbbr,
                        fontSizeSp = prefsRepo.fontSizeSp,
                        multiBibleReaderEnabled = prefsRepo.multiBibleReaderEnabled,
                    )
                }

                loadBooks(bibleAbbr, initialBookId, initialChapterId)
            } catch (e: Exception) {
                Log.e(TAG, "initialize error", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun loadBooks(abbr: String, bookId: String, chapterId: String) {
        val books = bibleRepo.getLocalBooks(abbr)
        if (books.isEmpty()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Bible data not available. Please wait for the download to complete."
                )
            }
            return
        }

        val targetBook = books.find { it.id == bookId } ?: books.first()
        _uiState.update { it.copy(books = books, activeBook = targetBook) }

        loadChapters(abbr, targetBook, chapterId)
    }

    private suspend fun loadChapters(abbr: String, book: BookEntity, chapterId: String) {
        val chapters = bibleRepo.getLocalChapters(abbr, book.id)
        if (chapters.isEmpty()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "No chapters found for ${book.name}"
                )
            }
            return
        }

        val targetChapter = chapters.find { it.id == chapterId } ?: chapters.first()
        _uiState.update { it.copy(chapters = chapters, activeChapter = targetChapter) }

        loadVerses(abbr, targetChapter)
    }

    suspend fun loadVerses(abbr: String, chapter: ChapterEntity) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        val verses = bibleRepo.getLocalVerses(abbr, chapter.id)
        if (verses == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Verses not cached. Please ensure download is complete."
                )
            }
            return
        }

        // Load parallel (secondary) bibles — respecting the Multi-Bible Reader toggle
        // and the user's chosen/ordered secondary-bible stack.
        val parallelMap = mutableMapOf<String, List<VerseDisplay>>()
        val multiBibleEnabled = prefsRepo.multiBibleReaderEnabled
        if (multiBibleEnabled) {
            val downloadedAbbrs = _uiState.value.savedBibles
                .filter { it.isDownloaded }
                .map { it.abbreviation }
                .toSet()

            val orderedSecondary = prefsRepo.getSecondaryBibleList()
                .filter { it != abbr && it in downloadedAbbrs }
                .ifEmpty {
                    // Fallback for users who haven't configured a stack yet.
                    _uiState.value.savedBibles
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

        _uiState.update {
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
            )
        }

        // Save prefs
        prefsRepo.lastBibleAbbr = abbr
        prefsRepo.lastBookId = chapter.bookId
        prefsRepo.lastChapterId = chapter.id

        // Record history
        val book = _uiState.value.activeBook
        if (book != null) {
            trackingRepo.recordReading(
                HistoryEntity(
                    bibleAbbr = abbr,
                    bookId = book.id,
                    bookName = book.name,
                    chapterId = chapter.id,
                    chapterRef = chapter.reference,
                )
            )
        }
    }

    fun selectBible(abbr: String) {
        val chapter = _uiState.value.activeChapter ?: return
        viewModelScope.launch {
            loadVerses(abbr, chapter)
            // Also reload books/chapters for the new bible
            loadBooks(abbr, _uiState.value.activeBook?.id ?: "", chapter.id)
        }
    }

    fun selectBook(book: BookEntity) {
        _uiState.update { it.copy(activeBook = book, chapters = emptyList(), verses = emptyList()) }
        viewModelScope.launch {
            loadChapters(_uiState.value.activeBibleAbbr, book, "")
        }
    }

    fun selectChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            loadVerses(_uiState.value.activeBibleAbbr, chapter)
        }
    }

    fun navigateChapter(direction: Int) {
        val chapters = _uiState.value.chapters
        val current = _uiState.value.activeChapter ?: return
        val idx = chapters.indexOfFirst { it.id == current.id }
        val next = chapters.getOrNull(idx + direction) ?: return
        selectChapter(next)
    }

    fun setFontSize(sp: Float) {
        prefsRepo.fontSizeSp = sp
        _uiState.update { it.copy(fontSizeSp = sp) }
    }

    /** Toggled from the reader's Quick Settings dialog; reloads verses so the parallel column updates immediately. */
    fun setMultiBibleReaderEnabled(enabled: Boolean) {
        prefsRepo.multiBibleReaderEnabled = enabled
        _uiState.update { it.copy(multiBibleReaderEnabled = enabled) }
        val chapter = _uiState.value.activeChapter ?: return
        viewModelScope.launch { loadVerses(_uiState.value.activeBibleAbbr, chapter) }
    }

    // ───────────────────────────── Bookmarks & Notes ─────────────────────────────

    /** Long-press (or continued tap while in selection mode) toggles a verse's selection. */
    fun toggleVerseSelected(verseId: String) {
        _uiState.update {
            val newSelection = if (verseId in it.selectedVerseIds) {
                it.selectedVerseIds - verseId
            } else {
                it.selectedVerseIds + verseId
            }
            it.copy(selectedVerseIds = newSelection)
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedVerseIds = emptySet(),
                showColorPicker = false,
                pendingHighlightColor = null,
            )
        }
    }

    /** A quick, single-verse bookmark toggle — triggered by swiping a verse right. */
    fun quickToggleBookmark(verseId: String) {
        val state = _uiState.value
        val abbr = state.activeBibleAbbr
        val bookId = state.activeBook?.id ?: return
        val chapterId = state.activeChapter?.id ?: return

        viewModelScope.launch {
            if (verseId in state.bookmarks) {
                annotationRepo.removeBookmarks(abbr, listOf(verseId))
                _uiState.update { it.copy(bookmarks = it.bookmarks - verseId) }
            } else {
                annotationRepo.setBookmarks(abbr, listOf(verseId), bookId, chapterId, colorHex = null)
                _uiState.update { it.copy(bookmarks = it.bookmarks + (verseId to null)) }
            }
        }
    }

    /** Swiping a verse left opens its notes screen directly. */
    fun requestNotesForVerse(verseId: String): NotesNavRequest? {
        val state = _uiState.value
        val verse = state.verses.find { it.verseId == verseId } ?: return null
        val request = buildNotesNavRequest(verse)
        _uiState.update { it.copy(notesNavRequest = request) }
        return request
    }

    fun consumeNotesNavRequest() {
        _uiState.update { it.copy(notesNavRequest = null) }
    }

    fun openColorPicker() {
        _uiState.update { it.copy(showColorPicker = true) }
    }

    fun dismissColorPicker() {
        _uiState.update { it.copy(showColorPicker = false) }
    }

    fun chooseHighlightColor(colorHex: String) {
        _uiState.update { it.copy(pendingHighlightColor = colorHex, showColorPicker = false) }
    }

    fun cancelPendingHighlight() {
        _uiState.update { it.copy(pendingHighlightColor = null) }
    }

    /** Opens notes directly for the single selected verse (top-bar "Notes" action). */
    fun openNotesForSelection(): NotesNavRequest? {
        val state = _uiState.value
        val verseId = state.selectedVerseIds.singleOrNull() ?: return null
        val verse = state.verses.find { it.verseId == verseId } ?: return null
        val request = buildNotesNavRequest(verse)
        _uiState.update { it.copy(notesNavRequest = request, selectedVerseIds = emptySet()) }
        return request
    }

    /** Applies the chosen highlight color to all selected verses, then exits selection mode. */
    fun confirmBookmarkOnly() {
        val state = _uiState.value
        val color = state.pendingHighlightColor ?: return
        val verseIds = state.selectedVerseIds
        val bookId = state.activeBook?.id ?: return
        val chapterId = state.activeChapter?.id ?: return

        viewModelScope.launch {
            annotationRepo.setBookmarks(state.activeBibleAbbr, verseIds, bookId, chapterId, color)
            _uiState.update {
                val updated = it.bookmarks.toMutableMap().apply {
                    verseIds.forEach { id -> put(id, color) }
                }
                it.copy(
                    bookmarks = updated,
                    selectedVerseIds = emptySet(),
                    pendingHighlightColor = null,
                )
            }
        }
    }

    /** Applies the highlight, then returns a nav request for the first selected verse's notes. */
    fun confirmBookmarkWithNotes(): NotesNavRequest? {
        val state = _uiState.value
        val color = state.pendingHighlightColor ?: return null
        val verseIds = state.selectedVerseIds
        val bookId = state.activeBook?.id ?: return null
        val chapterId = state.activeChapter?.id ?: return null
        val firstVerseId = verseIds.firstOrNull() ?: return null
        val verse = state.verses.find { it.verseId == firstVerseId } ?: return null

        val request = buildNotesNavRequest(verse)

        viewModelScope.launch {
            annotationRepo.setBookmarks(state.activeBibleAbbr, verseIds, bookId, chapterId, color)
        }

        _uiState.update {
            val updated = it.bookmarks.toMutableMap().apply {
                verseIds.forEach { id -> put(id, color) }
            }
            it.copy(
                bookmarks = updated,
                selectedVerseIds = emptySet(),
                pendingHighlightColor = null,
                notesNavRequest = request,
            )
        }
        return request
    }

    /** Called when returning from the Notes screen, to refresh the "has note" indicator. */
    fun refreshNotedVerses() {
        val abbr = _uiState.value.activeBibleAbbr
        val chapterId = _uiState.value.activeChapter?.id ?: return
        viewModelScope.launch {
            val noted = annotationRepo.getNotedVerseIds(abbr, chapterId)
            _uiState.update { it.copy(notedVerseIds = noted) }
        }
    }

    private fun buildNotesNavRequest(verse: VerseDisplay): NotesNavRequest {
        val state = _uiState.value
        val bookName = state.activeBook?.name ?: verse.bookId
        val chapterNumber = state.activeChapter?.number ?: ""
        val title = "$bookName $chapterNumber:${verse.number}"
        return NotesNavRequest(
            bibleAbbr = state.activeBibleAbbr,
            verseId = verse.verseId,
            bookId = state.activeBook?.id ?: verse.bookId,
            chapterId = state.activeChapter?.id ?: verse.chapterId,
            title = title,
            verseText = verse.text,
        )
    }

    companion object {
        private const val TAG = "ReaderViewModel"

        /** A small, friendly palette offered when bulk-highlighting verses. */
        val HIGHLIGHT_COLORS = listOf(
            "#FFF59D", // yellow
            "#A5D6A7", // green
            "#90CAF9", // blue
            "#F48FB1", // pink
            "#FFCC80", // orange
            "#CE93D8", // purple
        )
    }
}
