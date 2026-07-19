package com.biblelib.core.data.repos

import com.biblelib.core.database.model.ScriptureItemEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the scripture list currently "open" in the reader (the floating queue widget that
 * replaces the [ChapterNavBar]). This is in-memory/session-scoped — the persisted data lives
 * in [ScriptureRepo]; this repo just tracks which list (if any) is actively being read through,
 * and which item in it is currently on screen.
 */
@Singleton
class ScriptureQueueRepo @Inject constructor() {

    private val _listId = MutableStateFlow<Long?>(null)
    val listId: StateFlow<Long?> = _listId.asStateFlow()

    private val _listName = MutableStateFlow<String>("")
    val listName: StateFlow<String> = _listName.asStateFlow()

    private val _items = MutableStateFlow<List<ScriptureItemEntity>>(emptyList())
    val items: StateFlow<List<ScriptureItemEntity>> = _items.asStateFlow()

    private val _activeItemId = MutableStateFlow<Long?>(null)
    val activeItemId: StateFlow<Long?> = _activeItemId.asStateFlow()

    /** True while a queue is open and should be shown in place of the [ChapterNavBar]. */
    val isOpen: Boolean get() = _items.value.isNotEmpty()

    fun open(listId: Long, listName: String, items: List<ScriptureItemEntity>, activeItemId: Long? = items.firstOrNull()?.id) {
        _listId.value = listId
        _listName.value = listName
        _items.value = items
        _activeItemId.value = activeItemId
    }

    fun setActiveItem(itemId: Long) {
        if (_items.value.any { it.id == itemId }) {
            _activeItemId.value = itemId
        }
    }

    /** Marks whichever item best matches the chapter currently on screen as active, so the
     *  floating widget stays in sync as the user navigates chapters normally. */
    fun syncActiveByChapter(bibleAbbr: String, chapterId: String) {
        val match = _items.value.firstOrNull { it.bibleAbbr == bibleAbbr && it.chapterId == chapterId }
        if (match != null) _activeItemId.value = match.id
    }

    fun dismiss() {
        _listId.value = null
        _listName.value = ""
        _items.value = emptyList()
        _activeItemId.value = null
    }
}
