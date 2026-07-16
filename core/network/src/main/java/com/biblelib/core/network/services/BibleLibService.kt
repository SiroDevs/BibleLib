package com.biblelib.core.network.services

import androidx.annotation.Keep
import com.biblelib.core.network.dtos.BibleInfoDto
import com.biblelib.core.network.dtos.BooksResponse
import com.biblelib.core.network.dtos.ChaptersResponse
import com.biblelib.core.network.dtos.VersesResponse
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

    @GET("{abbr}/verses/{bookId}.json")
    suspend fun getVersesForBook(
        @Path("abbr") abbr: String,
        @Path("bookId") bookId: String,
    ): VersesResponse
}
