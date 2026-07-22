package com.biblelib.feature.reader.main.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.biblelib.core.common.entity.*
import com.biblelib.core.data.repos.AnnotationRepo
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ScriptureQueueRepo
import com.biblelib.core.data.repos.TrackingRepo
import com.biblelib.core.data.worker.SyncScheduler
import com.biblelib.core.data.worker.SyncWorker
import com.biblelib.core.database.model.BookEntity
import com.biblelib.core.database.model.ChapterEntity
import com.biblelib.core.database.model.BibleEntity
import com.biblelib.core.database.model.HistoryEntity
import com.biblelib.core.database.model.ScriptureItemEntity
import com.biblelib.feature.reader.main.utils.NotesNavRequest
import com.biblelib.feature.reader.main.utils.ReaderUiState
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val bibleRepo: BibleRepo,
    private val trackingRepo: TrackingRepo,
    private val prefsRepo: PrefsRepo,
    private val annotationRepo: AnnotationRepo,
    private val scriptureQueueRepo: ScriptureQueueRepo,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private val workManager = WorkManager.getInstance(context)
    private val observedAbbrs = mutableSetOf<String>()

    private var isFirstLoad = false

    init {
        viewModelScope.launch {
            combine(scriptureQueueRepo.items, scriptureQueueRepo.activeItemId) { items, activeId -> items to activeId }
                .collect { (items, activeId) ->
                    _uiState.update { it.copy(queueItems = items, queueActiveItemId = activeId) }
                }
        }
    }

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

                isFirstLoad = true

                _uiState.update {
                    it.copy(
                        savedBibles = bibles,
                        activeBible = bibleName,
                        activeBibleAbbr = bibleAbbr,
                        fontSizeSp = prefsRepo.fontSizeSp,
                        fontFamilyId = prefsRepo.readerFontFamily,
                        readerBackgroundId = prefsRepo.readerBackground,
                        multiBibleReaderEnabled = prefsRepo.multiBibleReaderEnabled,
                    )
                }

                loadBooks(bibleAbbr, initialBookId, initialChapterId)
                observeDownloads(bibles)
            } catch (e: Exception) {
                Log.e(TAG, "initialize error", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun observeDownloads(bibles: List<BibleEntity>) {
        bibles
            .filter { !it.isDownloaded && it.abbreviation !in observedAbbrs }
            .forEach { bible ->
                observedAbbrs += bible.abbreviation
                viewModelScope.launch {
                    workManager
                        .getWorkInfosByTagFlow(bible.abbreviation)
                        .collect { infos ->
                            val info = infos
                                .filterNot { it.state.isFinished }
                                .maxByOrNull { it.id.hashCode() }
                                ?: infos.maxByOrNull { it.id.hashCode() }
                            val progress = info?.progress?.getFloat(SyncWorker.KEY_PROGRESS, 0f) ?: 0f
                            if (progress > 0f) {
                                _uiState.update {
                                    it.copy(downloadProgress = it.downloadProgress + (bible.abbreviation to progress))
                                }
                            }
                            if (info != null && info.state.isFinished) {
                                observedAbbrs -= bible.abbreviation
                                _uiState.update {
                                    it.copy(savedBibles = bibleRepo.getbibles())
                                }
                            }
                        }
                }
            }
    }

    fun retryBibleDownload(abbr: String) {
        observedAbbrs -= abbr
        SyncScheduler.scheduleSecondaryDownload(context, abbr)
        viewModelScope.launch {
            observeDownloads(listOf(BibleEntity(
                abbreviation = abbr, name = "", description = "", languageName = "",
                scriptDirection = "LTR", copyright = "",
            )))
        }
    }

    fun restartBibleDownload(abbr: String) {
        observedAbbrs -= abbr
        SyncScheduler.cancelDownload(context, abbr)
        viewModelScope.launch {
            bibleRepo.clearBibleContent(abbr)
            SyncScheduler.scheduleSecondaryDownload(context, abbr)
            observeDownloads(listOf(BibleEntity(
                abbreviation = abbr, name = "", description = "", languageName = "",
                scriptDirection = "LTR", copyright = "",
            )))
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

        val restoreTarget = if (isFirstLoad) prefsRepo.lastVerseId.takeIf { it.isNotEmpty() } else null
        isFirstLoad = false

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
                restoreVerseId = restoreTarget,
            )
        }

        prefsRepo.lastBibleAbbr = abbr
        prefsRepo.lastBookId = chapter.bookId
        prefsRepo.lastChapterId = chapter.id

        scriptureQueueRepo.syncActiveByChapter(abbr, chapter.id)

        val book = _uiState.value.activeBook
        if (book != null) {
            trackingRepo.recordReading(
                HistoryEntity(
                    bibleAbbr = abbr,
                    bibleName = _uiState.value.activeBible,
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
        val chapter = _uiState.value.activeChapter ?: return
        val book = _uiState.value.activeBook ?: return
        val abbr = _uiState.value.activeBibleAbbr

        prefsRepo.lastVerseId = verseId

        viewModelScope.launch {
            trackingRepo.recordReading(
                HistoryEntity(
                    bibleAbbr = abbr,
                    bibleName = _uiState.value.activeBible,
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
        _uiState.update { it.copy(restoreVerseId = null) }
    }

    fun selectBible(abbr: String) {
        val chapter = _uiState.value.activeChapter ?: return
        val newName = _uiState.value.savedBibles.find { it.abbreviation == abbr }?.name
            ?: _uiState.value.activeBible

        prefsRepo.primaryBible = abbr
        prefsRepo.lastBible = newName
        prefsRepo.setSecondaryBibleList(prefsRepo.getSecondaryBibleList() - abbr)

        _uiState.update { it.copy(activeBible = newName, activeBibleAbbr = abbr) }

        viewModelScope.launch {
            loadVerses(abbr, chapter)
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

    /** Jumps the reader to a specific item from the currently open scripture queue, staying on
     *  whichever Bible is currently active (so a prior Bible switch is respected). */
    fun jumpToQueueItem(item: ScriptureItemEntity) {
        scriptureQueueRepo.setActiveItem(item.id)
        val abbr = _uiState.value.activeBibleAbbr
        viewModelScope.launch {
            loadBooks(abbr, item.bookId, item.chapterId)
            _uiState.update { it.copy(restoreVerseId = item.verseId) }
            prefsRepo.lastVerseId = item.verseId
        }
    }

    /** Closes the floating scripture queue and returns the reader to normal navigation. */
    fun dismissScriptureQueue() {
        scriptureQueueRepo.dismiss()
    }

    fun setFontSize(sp: Float) {
        prefsRepo.fontSizeSp = sp
        _uiState.update { it.copy(fontSizeSp = sp) }
    }

    fun setFontFamily(id: String) {
        prefsRepo.readerFontFamily = id
        _uiState.update { it.copy(fontFamilyId = id) }
    }

    fun setReaderBackground(id: String) {
        prefsRepo.readerBackground = id
        _uiState.update { it.copy(readerBackgroundId = id) }
    }

    fun setMultiBibleReaderEnabled(enabled: Boolean) {
        prefsRepo.multiBibleReaderEnabled = enabled
        _uiState.update { it.copy(multiBibleReaderEnabled = enabled) }
        val chapter = _uiState.value.activeChapter ?: return
        viewModelScope.launch { loadVerses(_uiState.value.activeBibleAbbr, chapter) }
    }

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

    fun openNotesForSelection(): NotesNavRequest? {
        val state = _uiState.value
        val verseId = state.selectedVerseIds.singleOrNull() ?: return null
        val verse = state.verses.find { it.verseId == verseId } ?: return null
        val request = buildNotesNavRequest(verse)
        _uiState.update { it.copy(notesNavRequest = request, selectedVerseIds = emptySet()) }
        return request
    }

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
