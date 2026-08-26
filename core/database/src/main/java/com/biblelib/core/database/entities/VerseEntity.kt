package com.biblelib.core.database.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "verses",
    indices = [Index("bibleAbbr"), Index("bookId"), Index("chapterId")],
    primaryKeys = ["chapterId", "bibleAbbr"]
)
data class VerseEntity(
    val chapterId: String,
    val bibleAbbr: String,
    val bookId: String,
    val verseCount: Int,
    val contentJson: String,
    val cachedAt: Long = System.currentTimeMillis(),
)
