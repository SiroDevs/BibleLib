package com.biblelib.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.biblelib.core.database.model.ScriptureItemEntity

@Dao
interface ScriptureItemDao {
    @Insert
    suspend fun insertAll(items: List<ScriptureItemEntity>)

    @Query("SELECT * FROM scripture_items WHERE listId = :listId ORDER BY sortOrder ASC")
    suspend fun getForList(listId: Long): List<ScriptureItemEntity>

    @Query("SELECT COUNT(*) FROM scripture_items WHERE listId = :listId")
    suspend fun countForList(listId: Long): Int

    @Query("DELETE FROM scripture_items WHERE listId = :listId")
    suspend fun deleteForList(listId: Long)

    @Query("DELETE FROM scripture_items")
    suspend fun deleteAll()
}
