package com.biblelib.core.data.repos

import com.biblelib.core.database.daos.BookmarkDao
import com.biblelib.core.database.daos.NoteDao
import com.biblelib.core.database.model.BookmarkEntity
import com.biblelib.core.database.model.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnotationRepo @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val noteDao: NoteDao,
) {
    suspend fun getBookmarksForChapter(abbr: String, chapterId: String): Map<String, String?> =
        withContext(Dispatchers.IO) {
            bookmarkDao.getForChapter(abbr, chapterId).associate { it.verseId to it.colorHex }
        }

    suspend fun getNotedVerseIds(abbr: String, chapterId: String): Set<String> =
        withContext(Dispatchers.IO) {
            noteDao.getVerseIdsForChapter(abbr, chapterId).toSet()
        }

    suspend fun setBookmarks(
        abbr: String,
        verseIds: Collection<String>,
        bookId: String,
        chapterId: String,
        colorHex: String? = null,
    ) = withContext(Dispatchers.IO) {
        bookmarkDao.insertAll(
            verseIds.map { verseId ->
                BookmarkEntity(
                    verseId = verseId,
                    bibleAbbr = abbr,
                    bookId = bookId,
                    chapterId = chapterId,
                    colorHex = colorHex,
                )
            }
        )
    }

    suspend fun removeBookmarks(abbr: String, verseIds: Collection<String>) =
        withContext(Dispatchers.IO) {
            bookmarkDao.deleteVerses(abbr, verseIds.toList())
        }

    suspend fun getNote(abbr: String, verseId: String): NoteEntity? =
        withContext(Dispatchers.IO) { noteDao.getForVerse(abbr, verseId) }

    suspend fun saveNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.upsert(note)
    }

    suspend fun deleteNote(abbr: String, verseId: String) = withContext(Dispatchers.IO) {
        noteDao.delete(abbr, verseId)
    }

    suspend fun getAllBookmarks(): List<BookmarkEntity> =
        withContext(Dispatchers.IO) { bookmarkDao.getAll() }

    suspend fun getAllNotes(): List<NoteEntity> =
        withContext(Dispatchers.IO) { noteDao.getAll() }

    suspend fun deleteBookmarks(items: Collection<BookmarkEntity>) = withContext(Dispatchers.IO) {
        items.groupBy { it.bibleAbbr }.forEach { (abbr, group) ->
            bookmarkDao.deleteVerses(abbr, group.map { it.verseId })
        }
    }

    suspend fun deleteNotes(items: Collection<NoteEntity>) = withContext(Dispatchers.IO) {
        items.forEach { noteDao.delete(it.bibleAbbr, it.verseId) }
    }

    suspend fun clearAllBookmarksAndNotes() = withContext(Dispatchers.IO) {
        bookmarkDao.deleteAll()
        noteDao.deleteAll()
    }

    companion object {
        val DEFAULT_BOOKMARK_COLOR: String? = null
    }
}
