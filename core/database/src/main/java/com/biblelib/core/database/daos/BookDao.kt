package com.biblelib.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.biblelib.core.database.model.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE bibleAbbr = :abbr ORDER BY sortOrder ASC")
    suspend fun getByBible(abbr: String): List<BookEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<BookEntity>)

    @Query("DELETE FROM books WHERE bibleAbbr = :abbr")
    suspend fun deleteByBible(abbr: String)

    @Query("DELETE FROM books")
    suspend fun deleteAll()
}
