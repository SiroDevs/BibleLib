package com.biblelib.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "histories")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bibleAbbr: String,
    val bookId: String,
    val bookName: String,
    val chapterId: String,
    val chapterRef: String,
    val readAt: Long = System.currentTimeMillis(),
)