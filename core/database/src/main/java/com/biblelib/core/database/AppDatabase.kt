package com.biblelib.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.biblelib.core.database.daos.BibleDao
import com.biblelib.core.database.daos.BookDao
import com.biblelib.core.database.daos.BookmarkDao
import com.biblelib.core.database.daos.ChapterDao
import com.biblelib.core.database.daos.NoteDao
import com.biblelib.core.database.daos.VerseDao
import com.biblelib.core.database.daos.SearchDao
import com.biblelib.core.database.daos.HistoryDao
import com.biblelib.core.database.model.BibleEntity
import com.biblelib.core.database.model.BookEntity
import com.biblelib.core.database.model.BookmarkEntity
import com.biblelib.core.database.model.ChapterEntity
import com.biblelib.core.database.model.NoteEntity
import com.biblelib.core.database.model.VerseEntity
import com.biblelib.core.database.model.HistoryEntity
import com.biblelib.core.database.model.SearchEntity

@Database(
    entities = [
        BibleEntity::class,
        BookEntity::class,
        ChapterEntity::class,
        VerseEntity::class,
        HistoryEntity::class,
        SearchEntity::class,
        BookmarkEntity::class,
        NoteEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun biblesDao(): BibleDao
    abstract fun booksDao(): BookDao
    abstract fun chaptersDao(): ChapterDao
    abstract fun versesDao(): VerseDao
    abstract fun historiesDao(): HistoryDao
    abstract fun searchesDao(): SearchDao
    abstract fun bookmarksDao(): BookmarkDao
    abstract fun notesDao(): NoteDao
}
