package com.biblelib.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.biblelib.core.database.model.ChapterEntity

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE bibleAbbr = :abbr AND bookId = :bookId ORDER BY CAST(number AS INTEGER) ASC")
    suspend fun getByBook(abbr: String, bookId: String): List<ChapterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<ChapterEntity>)

    @Query("DELETE FROM chapters WHERE bibleAbbr = :abbr")
    suspend fun deleteByBible(abbr: String)

    @Query("DELETE FROM chapters")
    suspend fun deleteAll()
}
