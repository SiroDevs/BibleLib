package com.biblelib.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.biblelib.core.database.model.NoteEntity

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE bibleAbbr = :abbr AND verseId = :verseId LIMIT 1")
    suspend fun getForVerse(abbr: String, verseId: String): NoteEntity?

    @Query("SELECT verseId FROM notes WHERE bibleAbbr = :abbr AND chapterId = :chapterId")
    suspend fun getVerseIdsForChapter(abbr: String, chapterId: String): List<String>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    suspend fun getAll(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity)

    @Query("DELETE FROM notes WHERE bibleAbbr = :abbr AND verseId = :verseId")
    suspend fun delete(abbr: String, verseId: String)

    @Query("DELETE FROM notes WHERE bibleAbbr = :abbr")
    suspend fun deleteByBible(abbr: String)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
