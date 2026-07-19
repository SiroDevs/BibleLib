package com.biblelib.core.common.entity

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

data class Selectable<T>(
    val data: T,
    val isSelected: Boolean = false
)

@Parcelize
@Serializable
data class BibleInfo(
    val name: String = "",
    val description: String = "",
    val abbreviation: String = "",
    val tagline: String = "",
    val language: BibleLanguage = BibleLanguage(),
    val copyright: String = "",
    val info: String = "",
) : Parcelable

@Parcelize
@Serializable
data class BibleLanguage(
    val id: String = "",
    val name: String = "",
    val script: String = "",
    val scriptDirection: String = "LTR",
) : Parcelable

@Parcelize
data class BibleBook(
    val id: String = "",
    val bibleId: String = "",
    val abbreviation: String = "",
    val name: String = "",
    val nameLong: String = "",
) : Parcelable

@Parcelize
data class BibleChapter(
    val id: String = "",
    val bibleId: String = "",
    val bookId: String = "",
    val number: String = "",
    val reference: String = "",
) : Parcelable

data class VerseContentItem(
    val name: String? = null,
    val type: String = "",
    val text: String? = null,
    val attrs: Map<String, String>? = null,
    val items: List<VerseContentItem>? = null,
)

data class ChapterContent(
    val id: String = "",
    val bibleId: String = "",
    val number: String = "",
    val bookId: String = "",
    val reference: String = "",
    val copyright: String = "",
    val verseCount: Int = 0,
    val content: List<VerseContentItem> = emptyList(),
)

data class VerseDisplay(
    val verseId: String = "",
    val number: Int = 1,
    val text: String = "",
    val chapterId: String = "",
    val bookId: String = "",
)

data class ReadingHistory(
    val bibleAbbr: String,
    val bookId: String,
    val bookName: String,
    val chapterId: String,
    val chapterRef: String,
    val readAt: Long = System.currentTimeMillis()
)

/** Everything needed to open the Reader at a precise verse, e.g. from the Scripture Opener. */
data class ScriptureNavTarget(
    val bibleAbbr: String,
    val bibleName: String,
    val bookId: String,
    val chapterId: String,
    val verseId: String,
)

data class SearchResult(
    val bibleAbbr: String,
    val bookId: String,
    val bookName: String,
    val chapterId: String,
    val verseId: String,
    val verseNumber: Int,
    val text: String,
    val query: String,
)
