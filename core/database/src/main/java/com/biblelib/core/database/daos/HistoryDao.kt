package com.biblelib.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.biblelib.core.database.model.HistoryEntity

@Dao
interface HistoryDao {
    @Query("SELECT * FROM histories ORDER BY readAt DESC LIMIT 100")
    suspend fun getRecent(): List<HistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: HistoryEntity)

    @Query("DELETE FROM histories WHERE id NOT IN (SELECT id FROM histories ORDER BY readAt DESC LIMIT 200)")
    suspend fun pruneOld()

    @Query("DELETE FROM histories")
    suspend fun deleteAll()
}