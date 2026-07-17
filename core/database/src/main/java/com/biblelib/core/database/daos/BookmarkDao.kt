package com.biblelib.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.biblelib.core.database.model.BookmarkEntity

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE bibleAbbr = :abbr AND chapterId = :chapterId")
    suspend fun getForChapter(abbr: String, chapterId: String): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    suspend fun getAll(): List<BookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<BookmarkEntity>)

    @Query("DELETE FROM bookmarks WHERE bibleAbbr = :abbr AND verseId IN (:verseIds)")
    suspend fun deleteVerses(abbr: String, verseIds: List<String>)

    @Query("DELETE FROM bookmarks WHERE bibleAbbr = :abbr")
    suspend fun deleteByBible(abbr: String)

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAll()
}
