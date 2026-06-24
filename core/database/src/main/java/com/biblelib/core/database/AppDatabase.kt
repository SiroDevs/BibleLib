package com.biblelib.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.biblelib.core.database.daos.BookDao
import com.biblelib.core.database.daos.DraftDao
import com.biblelib.core.database.daos.EditDao
import com.biblelib.core.database.daos.HistoryDao
import com.biblelib.core.database.daos.ListingDao
import com.biblelib.core.database.daos.SearchDao
import com.biblelib.core.database.daos.SongDao
import com.biblelib.core.database.model.BookEntity
import com.biblelib.core.database.model.DraftEntity
import com.biblelib.core.database.model.EditEntity
import com.biblelib.core.database.model.HistoryEntity
import com.biblelib.core.database.model.ListingEntity
import com.biblelib.core.database.model.SearchEntity
import com.biblelib.core.database.model.SongEntity

@Database(
    entities = [
        BookEntity::class,
        HistoryEntity::class,
        ListingEntity::class,
        SearchEntity::class,
        SongEntity::class,
        DraftEntity::class,
        EditEntity::class,
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun booksDao(): BookDao
    abstract fun historiesDao(): HistoryDao
    abstract fun listingsDao(): ListingDao
    abstract fun searchesDao(): SearchDao
    abstract fun songsDao(): SongDao
    abstract fun draftsDao(): DraftDao
    abstract fun editsDao(): EditDao
}
