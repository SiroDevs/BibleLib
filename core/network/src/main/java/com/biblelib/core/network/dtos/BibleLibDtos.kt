package com.biblelib.core.network.dtos

data class BibleInfoDto(
    val name: String = "",
    val description: String = "",
    val abbreviation: String = "",
    val tagline: String = "",
    val language: BibleLanguageDto = BibleLanguageDto(),
    val countries: List<BibleCountryDto> = emptyList(),
    val copyright: String = "",
    val info: String = "",
)

data class BibleLanguageDto(
    val id: String = "",
    val name: String = "",
    val script: String = "",
    val scriptDirection: String = "LTR",
)

data class BibleCountryDto(
    val id: String = "",
    val name: String = "",
)

data class BooksResponse(
    val data: List<BookDto> = emptyList()
)

data class BookDto(
    val id: String = "",
    val bibleId: String = "",
    val abbreviation: String = "",
    val name: String = "",
    val nameLong: String = "",
)

// chapters.json is a map of bookId -> List<ChapterDto>
// e.g. { "GEN": [ { "id": "GEN.1", ... }, ... ], "EXO": [...] }
typealias ChaptersResponse = Map<String, List<ChapterDto>>

data class ChapterDto(
    val id: String = "",
    val bibleId: String = "",
    val bookId: String = "",
    val number: String = "",
    val reference: String = "",
)

// verses.json is:  { "GEN": { "GEN.1": { ChapterContentDto }, ... } }
typealias VersesResponse = Map<String, Map<String, ChapterContentDto>>

data class ChapterContentDto(
    val id: String = "",
    val bibleId: String = "",
    val number: String = "",
    val bookId: String = "",
    val reference: String = "",
    val copyright: String = "",
    val verseCount: Int = 0,
    val content: List<ContentItemDto> = emptyList(),
)

data class ContentItemDto(
    val name: String? = null,
    val type: String = "",
    val text: String? = null,
    val attrs: Map<String, String>? = null,
    val items: List<ContentItemDto>? = null,
)
