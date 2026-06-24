package com.biblelib.core.database.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "chapters",
    indices = [Index("bibleAbbr"), Index("bookId")],
    primaryKeys = ["id", "bibleAbbr"]
)
data class ChapterEntity(
    val id: String,
    val bibleAbbr: String,
    val bookId: String,
    val number: String,
    val reference: String,
)
