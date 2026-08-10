package com.biblelib.core.network.services

import android.content.Context
import com.biblelib.core.network.dtos.BibleInfoDto
import com.biblelib.core.network.dtos.BooksResponse
import com.biblelib.core.network.dtos.ChapterContentDto
import com.biblelib.core.network.dtos.ChaptersResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bundled-assets implementation of [BibleLibService].
 *
 * TEMPORARY MEASURE: the remote BibleLib API (see [BibleLibRetrofitService] / [NetworkModule])
 * was causing downloads to fail in production. Until that's root-caused, the app reads a small
 * set of Bibles that are bundled directly inside the APK under `assets/bibles/`, using the exact
 * same JSON shapes the API used to return. This keeps the rest of the app (BibleRepo, download
 * flow, DB caching) completely unchanged.
 *
 * Expected asset layout (mirrors the old REST endpoints 1:1):
 *   assets/bibles/info.json                              -> List<BibleInfoDto>
 *   assets/bibles/{abbr}/books.json                       -> List<BookDto>
 *   assets/bibles/{abbr}/chapters.json                    -> Map<bookId, List<ChapterDto>>
 *   assets/bibles/{abbr}/verses/{bookId}/{chapter}.json    -> ChapterContentDto
 *
 * To restore live downloads later, swap [com.biblelib.core.network.di.NetworkModule]'s
 * `provideBibleLibService` back to the Retrofit-backed provider.
 */
@Singleton
class AssetBibleLibService @Inject constructor(
    private val context: Context,
) : BibleLibService {

    private val gson = Gson()

    override suspend fun getBiblesInfo(): List<BibleInfoDto> =
        readJson("bibles/info.json", object : TypeToken<List<BibleInfoDto>>() {}.type)

    override suspend fun getBooks(abbr: String): BooksResponse =
        readJson("bibles/$abbr/books.json", object : TypeToken<BooksResponse>() {}.type)

    override suspend fun getChapters(abbr: String): ChaptersResponse =
        readJson("bibles/$abbr/chapters.json", object : TypeToken<ChaptersResponse>() {}.type)

    override suspend fun getVersesForChapter(
        abbr: String,
        bookId: String,
        chapter: String,
    ): ChapterContentDto =
        readJson(
            "bibles/$abbr/verses/$bookId/$chapter.json",
            object : TypeToken<ChapterContentDto>() {}.type,
        )

    private suspend fun <T> readJson(assetPath: String, type: java.lang.reflect.Type): T =
        withContext(Dispatchers.IO) {
            try {
                context.assets.open(assetPath).bufferedReader().use { reader ->
                    gson.fromJson(reader, type)
                }
            } catch (e: IOException) {
                throw IOException("Missing bundled Bible asset: $assetPath", e)
            }
        }
}
