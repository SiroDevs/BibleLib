package com.biblelib.core.network.services

import androidx.annotation.Keep
import com.biblelib.core.network.dtos.BibleInfoDto
import com.biblelib.core.network.dtos.BooksResponse
import com.biblelib.core.network.dtos.ChapterContentDto
import com.biblelib.core.network.dtos.ChaptersResponse
import retrofit2.http.GET
import retrofit2.http.Path

@Keep
interface BibleLibService {

    @GET("info.json")
    suspend fun getBiblesInfo(): List<BibleInfoDto>

    @GET("{abbr}/books.json")
    suspend fun getBooks(@Path("abbr") abbr: String): BooksResponse

    @GET("{abbr}/chapters.json")
    suspend fun getChapters(@Path("abbr") abbr: String): ChaptersResponse

    // e.g. GET {abbr}/verses/RUT/1.json — book and chapter are now separate path segments.
    @GET("{abbr}/verses/{bookId}/{chapter}.json")
    suspend fun getVersesForChapter(
        @Path("abbr") abbr: String,
        @Path("bookId") bookId: String,
        @Path("chapter") chapter: String,
    ): ChapterContentDto
}
