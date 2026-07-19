package com.biblelib.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A single book/chapter/verse reference that belongs to a [ScriptureListEntity]. */
@Entity(
    tableName = "scripture_items",
    indices = [Index("listId")],
)
data class ScriptureItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val listId: Long = 0,
    val bibleAbbr: String,
    val bibleName: String,
    val bookId: String,
    val bookName: String,
    val bookAbbr: String,
    val chapterId: String,
    val chapterNumber: String,
    val verseId: String,
    val verseNumber: Int,
    /** Display label, e.g. "Genesis 1:1". */
    val reference: String,
    val sortOrder: Int = 0,
    val addedAt: Long = System.currentTimeMillis(),
)
