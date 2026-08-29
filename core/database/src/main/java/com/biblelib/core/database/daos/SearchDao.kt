package com.biblelib.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.biblelib.core.database.entities.SearchEntity

@Dao
interface SearchDao {
    @Query("SELECT * FROM searches ORDER BY queriedAt DESC LIMIT 50")
    suspend fun getRecent(): List<SearchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(search: SearchEntity)

    @Query("DELETE FROM searches WHERE qry = :query COLLATE NOCASE")
    suspend fun deleteByQuery(query: String)

    @Query("DELETE FROM searches WHERE id NOT IN (SELECT id FROM searches ORDER BY queriedAt DESC LIMIT 100)")
    suspend fun pruneOld()

    @Query("DELETE FROM searches")
    suspend fun deleteAll()
}
