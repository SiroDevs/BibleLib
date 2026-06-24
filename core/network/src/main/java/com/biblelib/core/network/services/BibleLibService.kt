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

    /** GET /bibles/info.json  → list of all available bible versions */
    @GET("info.json")
    suspend fun getBiblesInfo(): List<BibleInfoDto>

    /** GET /bibles/{abbr}/books.json  → { data: [ BookDto, ... ] } */
    @GET("{abbr}/books.json")
    suspend fun getBooks(@Path("abbr") abbr: String): BooksResponse

    /** GET /bibles/{abbr}/chapters.json  → { "GEN": [ ChapterDto, ... ], ... } */
    @GET("{abbr}/chapters.json")
    suspend fun getChapters(@Path("abbr") abbr: String): ChaptersResponse

    /** GET /bibles/{abbr}/verses.json  → { "GEN": { "GEN.1": ChapterContentDto } } */
    @GET("{abbr}/verses.json")
    suspend fun getVerses(@Path("abbr") abbr: String): VersesResponse
}
