package com.biblelib.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.biblelib.core.database.model.ScriptureListEntity

@Dao
interface ScriptureListDao {
    @Insert
    suspend fun insert(list: ScriptureListEntity): Long

    @Query("SELECT * FROM scripture_lists ORDER BY createdAt DESC")
    suspend fun getAll(): List<ScriptureListEntity>

    @Query("SELECT * FROM scripture_lists WHERE id = :listId LIMIT 1")
    suspend fun getById(listId: Long): ScriptureListEntity?

    @Query("UPDATE scripture_lists SET name = :name WHERE id = :listId")
    suspend fun rename(listId: Long, name: String)

    @Query("DELETE FROM scripture_lists WHERE id = :listId")
    suspend fun delete(listId: Long)

    @Query("DELETE FROM scripture_lists")
    suspend fun deleteAll()
}
