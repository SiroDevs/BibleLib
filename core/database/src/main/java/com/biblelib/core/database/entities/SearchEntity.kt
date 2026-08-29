package com.biblelib.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "searches")
data class SearchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val qry: String,
    val queriedAt: Long = System.currentTimeMillis(),
)
