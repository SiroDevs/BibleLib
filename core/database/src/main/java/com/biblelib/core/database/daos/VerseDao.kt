package com.biblelib.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.biblelib.core.database.model.VerseEntity

@Dao
interface VerseDao {
    @Query("SELECT * FROM verses WHERE bibleAbbr = :abbr AND chapterId = :chapterId LIMIT 1")
    suspend fun getChapter(abbr: String, chapterId: String): VerseEntity?

    @Query("SELECT chapterId FROM verses WHERE bibleAbbr = :abbr")
    suspend fun getCachedChapterIds(abbr: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(verse: VerseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<VerseEntity>)

    @Query("DELETE FROM verses WHERE bibleAbbr = :abbr")
    suspend fun deleteByBible(abbr: String)

    @Query("DELETE FROM verses")
    suspend fun deleteAll()

    @Query("""
        SELECT * FROM verses 
        WHERE bibleAbbr = :abbr AND contentJson LIKE '%' || :query || '%'
    """)
    suspend fun searchInBible(abbr: String, query: String): List<VerseEntity>
}