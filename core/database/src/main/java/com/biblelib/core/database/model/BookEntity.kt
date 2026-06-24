package com.biblelib.core.database.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "books",
    indices = [Index("bibleAbbr")],
    primaryKeys = ["id", "bibleAbbr"]
)
data class BookEntity(
    val id: String,
    val bibleAbbr: String,
    val abbreviation: String,
    val name: String,
    val nameLong: String,
    val sortOrder: Int = 0,
)
