package com.biblelib.feature.reader.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.biblelib.core.common.entity.*
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.TrackingRepo
import com.biblelib.core.database.model.BookEntity
import com.biblelib.core.database.model.ChapterEntity
import com.biblelib.core.database.model.BibleEntity
import com.biblelib.core.database.model.HistoryEntity
import javax.inject.Inject

data class ReaderUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val savedBibles: List<BibleEntity> = emptyList(),
    val activeBibleAbbr: String = "",
    val books: List<BookEntity> = emptyList(),
    val activeBook: BookEntity? = null,
    val chapters: List<ChapterEntity> = emptyList(),
    val activeChapter: ChapterEntity? = null,
    val verses: List<VerseDisplay> = emptyList(),
    val parallelVerses: Map<String, List<VerseDisplay>> = emptyMap(),
    val fontSizeSp: Float = 18f,
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val bibleRepo: BibleRepo,
    private val trackingRepo: TrackingRepo,
    private val prefsRepo: PrefsRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun initialize(initialBibleAbbr: String, initialBookId: String, initialChapterId: String) {
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

                // Resolve which bible / book / chapter to open
                val bibleAbbr = initialBibleAbbr.ifEmpty {
                    prefsRepo.lastBibleAbbr.ifEmpty { bibles.first().abbreviation }
                }

                _uiState.update {
                    it.copy(
                        savedBibles = bibles,
                        activeBibleAbbr = bibleAbbr,
                        fontSizeSp = prefsRepo.fontSizeSp,
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

        // Load parallel bibles
        val parallelMap = mutableMapOf<String, List<VerseDisplay>>()
        _uiState.value.savedBibles
            .filter { it.abbreviation != abbr && it.isDownloaded }
            .forEach { saved ->
                val pVerses = bibleRepo.getLocalVerses(saved.abbreviation, chapter.id)
                if (pVerses != null) parallelMap[saved.abbreviation] = pVerses
            }

        _uiState.update {
            it.copy(
                isLoading = false,
                verses = verses,
                parallelVerses = parallelMap,
                activeChapter = chapter,
                activeBibleAbbr = abbr,
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

    companion object {
        private const val TAG = "ReaderViewModel"
    }
}
