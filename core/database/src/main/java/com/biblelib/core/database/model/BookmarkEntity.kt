package com.biblelib.core.database.model

import androidx.room.Entity
import androidx.room.Index

/**
 * A bookmarked verse.
 *
 * [colorHex] is null for a "quick" single-verse bookmark (swipe action) — shown in the
 * reader as a small bookmark icon next to the verse. When a color is set (chosen via the
 * multi-select highlight flow) the verse row is rendered with that color as a background wash.
 */
@Entity(
    tableName = "bookmarks",
    indices = [Index("bibleAbbr"), Index("chapterId")],
    primaryKeys = ["verseId", "bibleAbbr"]
)
data class BookmarkEntity(
    val verseId: String,
    val bibleAbbr: String,
    val bookId: String,
    val chapterId: String,
    val colorHex: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
