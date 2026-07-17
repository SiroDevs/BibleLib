package com.biblelib.core.database.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "notes",
    indices = [Index("bibleAbbr"), Index("chapterId")],
    primaryKeys = ["verseId", "bibleAbbr"]
)
data class NoteEntity(
    val verseId: String,
    val bibleAbbr: String,
    val bookId: String,
    val chapterId: String,
    val title: String,
    val verseText: String,
    val noteText: String,
    val updatedAt: Long = System.currentTimeMillis(),
)
