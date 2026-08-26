package com.biblelib.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "searches")
data class SearchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val query: String,
    val searchedAt: Long = System.currentTimeMillis(),
)
