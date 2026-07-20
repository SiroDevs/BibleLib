package com.biblelib.feature.scriptureopener.opener.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblelib.core.common.entity.ScriptureNavTarget
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ScriptureQueueRepo
import com.biblelib.core.data.repos.ScriptureRepo
import com.biblelib.core.database.model.BookEntity
import com.biblelib.core.database.model.ChapterEntity
import com.biblelib.core.database.model.ScriptureItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScriptureOpenerUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val bibleAbbr: String = "",
    val bibleName: String = "",
    val rows: List<ScriptureSearchRowState> = emptyList(),
    val navigateToReader: ScriptureNavTarget? = null,
    val closeRequested: Boolean = false,
) {
    val activeRow: ScriptureSearchRowState? get() = rows.lastOrNull { !it.locked }
}

@HiltViewModel
class ScriptureOpenerViewModel @Inject constructor(
    private val bibleRepo: BibleRepo,
    private val scriptureRepo: ScriptureRepo,
    private val scriptureQueueRepo: ScriptureQueueRepo,
    private val prefsRepo: PrefsRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScriptureOpenerUiState())
    val uiState: StateFlow<ScriptureOpenerUiState> = _uiState.asStateFlow()

    fun initialize(bibleAbbr: String, bibleName: String) {
        if (_uiState.value.bibleAbbr == bibleAbbr && _uiState.value.rows.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, bibleAbbr = bibleAbbr, bibleName = bibleName) }
            val books = bibleRepo.getLocalBooks(bibleAbbr)
            if (books.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, error = "No books found for this Bible.") }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    rows = listOf(ScriptureSearchRowState(books = books)),
                )
            }
        }
    }

    // ───────────────────────────── Row expand/collapse ─────────────────────────────

    fun toggleField(rowKey: String, field: ExpandedField) {
        updateRow(rowKey) { row ->
            if (row.locked) return@updateRow row
            val allowed = when (field) {
                ExpandedField.BOOK -> true
                ExpandedField.CHAPTER -> row.canExpandChapter
                ExpandedField.VERSE -> row.canExpandVerse
                ExpandedField.NONE -> true
            }
            if (!allowed) return@updateRow row
            row.copy(expanded = if (row.expanded == field) ExpandedField.NONE else field)
        }
    }

    fun closeResults(rowKey: String) {
        updateRow(rowKey) { it.copy(expanded = ExpandedField.NONE) }
    }

    // ───────────────────────────── Selections ─────────────────────────────

    fun selectBook(rowKey: String, book: BookEntity) {
        updateRow(rowKey) { row ->
            row.copy(
                selectedBook = book,
                selectedChapter = null,
                chapters = emptyList(),
                selectedVerseNumber = null,
                verses = emptyList(),
                isLoadingChapters = true,
                expanded = ExpandedField.CHAPTER,
            )
        }
        viewModelScope.launch {
            val abbr = _uiState.value.bibleAbbr
            val chapters = bibleRepo.getLocalChapters(abbr, book.id)
            updateRow(rowKey) { it.copy(chapters = chapters, isLoadingChapters = false) }
        }
    }

    fun selectChapter(rowKey: String, chapter: ChapterEntity) {
        updateRow(rowKey) { row ->
            row.copy(
                selectedChapter = chapter,
                selectedVerseNumber = null,
                verses = emptyList(),
                isLoadingVerses = true,
                expanded = ExpandedField.VERSE,
            )
        }
        viewModelScope.launch {
            val abbr = _uiState.value.bibleAbbr
            val verses = bibleRepo.getLocalVerses(abbr, chapter.id).orEmpty()
            updateRow(rowKey) { it.copy(verses = verses, isLoadingVerses = false) }
        }
    }

    fun selectVerse(rowKey: String, number: Int) {
        updateRow(rowKey) { it.copy(selectedVerseNumber = number, expanded = ExpandedField.NONE) }
    }

    // ───────────────────────────── Actions ─────────────────────────────

    /** Opens the searched scripture right away, without touching the saved queue. */
    fun openScripture(rowKey: String) {
        val row = _uiState.value.rows.firstOrNull { it.key == rowKey } ?: return
        if (!row.isComplete) return
        val target = buildTarget(row) ?: return
        prefsRepo.lastVerseId = target.verseId
        _uiState.update { it.copy(navigateToReader = target) }
    }

    /** Locks the current row into the queue and reveals a fresh blank row beneath it. */
    fun addToQueue(rowKey: String) {
        val state = _uiState.value
        val row = state.rows.firstOrNull { it.key == rowKey } ?: return
        if (!row.isComplete) return

        _uiState.update {
            val updatedRows = it.rows.map { r -> if (r.key == rowKey) r.copy(locked = true, expanded = ExpandedField.NONE) else r }
            it.copy(rows = updatedRows + ScriptureSearchRowState(books = row.books))
        }
    }

    /** Locks + persists the queue, then returns to the reader screen exactly as it was. */
    fun addToQueueAndClose(rowKey: String) {
        val row = _uiState.value.rows.firstOrNull { it.key == rowKey } ?: return
        if (!row.isComplete) return

        viewModelScope.launch {
            persistQueue(includingRowKey = rowKey)
            _uiState.update { it.copy(closeRequested = true) }
        }
    }

    /** Locks + persists the queue, then opens the reader at the first scripture in it. */
    fun addToQueueAndFinish(rowKey: String) {
        val row = _uiState.value.rows.firstOrNull { it.key == rowKey } ?: return
        if (!row.isComplete) return

        viewModelScope.launch {
            val items = persistQueue(includingRowKey = rowKey)
            val first = items.firstOrNull() ?: return@launch
            prefsRepo.lastVerseId = first.verseId
            _uiState.update {
                it.copy(
                    navigateToReader = ScriptureNavTarget(
                        bibleAbbr = first.bibleAbbr,
                        bibleName = first.bibleName,
                        bookId = first.bookId,
                        chapterId = first.chapterId,
                        verseId = first.verseId,
                    )
                )
            }
        }
    }

    /** Builds the full item list (all locked rows + the given row), saves it, and opens it
     *  in the [ScriptureQueueRepo] so the reader's floating widget picks it up. */
    private suspend fun persistQueue(includingRowKey: String): List<ScriptureItemEntity> {
        val state = _uiState.value
        val completedRows = state.rows.filter { it.locked || it.key == includingRowKey }
        val items = completedRows.mapIndexedNotNull { index, row -> buildItem(row, index) }
        if (items.isEmpty()) return emptyList()

        val listId = scriptureRepo.saveList(items)
        val saved = scriptureRepo.getItems(listId)
        val list = scriptureRepo.getList(listId)
        scriptureQueueRepo.open(listId, list?.name ?: saved.first().reference, saved)
        return saved
    }

    private fun buildItem(row: ScriptureSearchRowState, order: Int): ScriptureItemEntity? {
        val book = row.selectedBook ?: return null
        val chapter = row.selectedChapter ?: return null
        val verseNumber = row.selectedVerseNumber ?: return null
        val verseId = row.selectedVerseId ?: return null
        val state = _uiState.value
        return ScriptureItemEntity(
            listId = 0,
            bibleAbbr = state.bibleAbbr,
            bibleName = state.bibleName,
            bookId = book.id,
            bookName = book.name,
            bookAbbr = book.abbreviation,
            chapterId = chapter.id,
            chapterNumber = chapter.number,
            verseId = verseId,
            verseNumber = verseNumber,
            reference = "${book.name} ${chapter.number}:$verseNumber",
            sortOrder = order,
        )
    }

    private fun buildTarget(row: ScriptureSearchRowState): ScriptureNavTarget? {
        val book = row.selectedBook ?: return null
        val chapter = row.selectedChapter ?: return null
        val verseId = row.selectedVerseId ?: return null
        val state = _uiState.value
        return ScriptureNavTarget(
            bibleAbbr = state.bibleAbbr,
            bibleName = state.bibleName,
            bookId = book.id,
            chapterId = chapter.id,
            verseId = verseId,
        )
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(navigateToReader = null) }
    }

    fun consumeClose() {
        _uiState.update { it.copy(closeRequested = false) }
    }

    private inline fun updateRow(rowKey: String, transform: (ScriptureSearchRowState) -> ScriptureSearchRowState) {
        _uiState.update { state ->
            state.copy(rows = state.rows.map { if (it.key == rowKey) transform(it) else it })
        }
    }
}
