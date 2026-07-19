package com.biblelib.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.biblelib.core.database.model.BibleEntity

@Dao
interface BibleDao {
    @Query("SELECT * FROM bibles ORDER BY sortOrder ASC")
    suspend fun getAll(): List<BibleEntity>

    @Query("SELECT * FROM bibles WHERE abbreviation = :abbr LIMIT 1")
    suspend fun getByAbbr(abbr: String): BibleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bible: BibleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<BibleEntity>)

    @Delete
    suspend fun delete(bible: BibleEntity)

    @Query("DELETE FROM bibles WHERE abbreviation = :abbr")
    suspend fun deleteByAbbr(abbr: String)

    @Query("DELETE FROM bibles")
    suspend fun deleteAll()

    @Query("UPDATE bibles SET isDownloaded = 1, downloadFailed = 0, downloadProgress = 1.0 WHERE abbreviation = :abbr")
    suspend fun markDownloaded(abbr: String)

    @Query("UPDATE bibles SET downloadProgress = :progress, downloadFailed = 0 WHERE abbreviation = :abbr")
    suspend fun updateProgress(abbr: String, progress: Float)

    @Query("UPDATE bibles SET downloadFailed = 1, downloadProgress = :progress WHERE abbreviation = :abbr")
    suspend fun markFailed(abbr: String, progress: Float)

    @Query("UPDATE bibles SET downloadFailed = 0 WHERE abbreviation = :abbr")
    suspend fun clearFailed(abbr: String)
}