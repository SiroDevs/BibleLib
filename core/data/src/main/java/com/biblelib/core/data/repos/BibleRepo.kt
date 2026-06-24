package com.biblelib.core.data.repos

import android.util.Log
import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.core.database.daos.BibleDao
import com.biblelib.core.database.daos.BookDao
import com.biblelib.core.database.daos.ChapterDao
import com.biblelib.core.database.daos.VerseDao
import com.biblelib.core.database.model.BibleEntity
import com.biblelib.core.database.model.BookEntity
import com.biblelib.core.database.model.ChapterEntity
import com.biblelib.core.database.model.VerseEntity
import com.biblelib.core.network.dtos.BibleInfoDto
import com.biblelib.core.network.dtos.ChapterContentDto
import com.biblelib.core.network.dtos.ContentItemDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.biblelib.core.network.services.BibleLibService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BibleRepo @Inject constructor(
    private val service: BibleLibService,
    private val bibleDao: BibleDao,
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
) {
    private val gson = Gson()

    suspend fun fetchAvailableBibles(): List<BibleInfoDto> =
        withContext(Dispatchers.IO) {
            service.getBiblesInfo()
        }

    suspend fun downloadBible(
        abbr: String,
        onProgress: suspend (step: String, progress: Float) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        Log.d(TAG, "▶ Downloading bible: $abbr")

        onProgress("Fetching books…", 0.1f)
        val booksResp = service.getBooks(abbr)
        val bookEntities = booksResp.data.mapIndexed { i, dto ->
            BookEntity(
                id = dto.id,
                bibleAbbr = abbr,
                abbreviation = dto.abbreviation,
                name = dto.name,
                nameLong = dto.nameLong,
                sortOrder = i,
            )
        }
        bookDao.insertAll(bookEntities)
        Log.d(TAG, "✅ ${bookEntities.size} books saved for $abbr")

        onProgress("Fetching chapters…", 0.3f)
        val chaptersResp = service.getChapters(abbr)
        val chapterEntities = mutableListOf<ChapterEntity>()
        chaptersResp.forEach { (_, chapters) ->
            chapters.forEach { dto ->
                chapterEntities.add(
                    ChapterEntity(
                        id = dto.id,
                        bibleAbbr = abbr,
                        bookId = dto.bookId,
                        number = dto.number,
                        reference = dto.reference,
                    )
                )
            }
        }
        chapterDao.insertAll(chapterEntities)
        Log.d(TAG, "✅ ${chapterEntities.size} chapters saved for $abbr")

        onProgress("Fetching verses…", 0.6f)
        val versesResp = service.getVerses(abbr)
        val verseEntities = mutableListOf<VerseEntity>()
        versesResp.forEach { (bookId, chapters) ->
            chapters.forEach { (chapterId, content) ->
                val verses = extractVerses(content)
                val json = gson.toJson(verses)
                verseEntities.add(
                    VerseEntity(
                        chapterId = chapterId,
                        bibleAbbr = abbr,
                        bookId = bookId,
                        verseCount = content.verseCount,
                        copyright = content.copyright,
                        contentJson = json,
                    )
                )
            }
        }
        verseDao.insertAll(verseEntities)
        Log.d(TAG, "✅ ${verseEntities.size} chapter verse sets saved for $abbr")

        bibleDao.markDownloaded(abbr)
        onProgress("Done!", 1.0f)
        Log.d(TAG, "✅ Download complete for $abbr")
    }

    // ─── Local reads ─────────────────────────────────────────────────────────

    suspend fun getLocalBooks(abbr: String): List<BookEntity> =
        withContext(Dispatchers.IO) { bookDao.getByBible(abbr) }

    suspend fun getLocalChapters(abbr: String, bookId: String): List<ChapterEntity> =
        withContext(Dispatchers.IO) { chapterDao.getByBook(abbr, bookId) }

    suspend fun getLocalVerses(abbr: String, chapterId: String): List<VerseDisplay>? =
        withContext(Dispatchers.IO) {
            val entity = verseDao.getChapter(abbr, chapterId) ?: return@withContext null
            val type = object : TypeToken<List<VerseDisplay>>() {}.type
            gson.fromJson<List<VerseDisplay>>(entity.contentJson, type)
        }

    suspend fun searchVerses(abbr: String, query: String): List<VerseDisplay> =
        withContext(Dispatchers.IO) {
            val entities = verseDao.searchInBible(abbr, query)
            val type = object : TypeToken<List<VerseDisplay>>() {}.type
            entities.flatMap { entity ->
                val verses: List<VerseDisplay> = gson.fromJson(entity.contentJson, type)
                verses.filter { it.text.contains(query, ignoreCase = true) }
            }
        }

    suspend fun getbibles(): List<BibleEntity> =
        withContext(Dispatchers.IO) { bibleDao.getAll() }

    suspend fun saveBible(entity: BibleEntity) =
        withContext(Dispatchers.IO) { bibleDao.insert(entity) }

    suspend fun saveBibles(entities: List<BibleEntity>) =
        withContext(Dispatchers.IO) { bibleDao.insertAll(entities) }

    suspend fun deleteBible(abbr: String) = withContext(Dispatchers.IO) {
        bibleDao.deleteByAbbr(abbr)
        bookDao.deleteByBible(abbr)
        chapterDao.deleteByBible(abbr)
        verseDao.deleteByBible(abbr)
    }

    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        bibleDao.deleteAll()
        bookDao.deleteAll()
        chapterDao.deleteAll()
        verseDao.deleteAll()
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Walk the nested content tree from the API and extract flat verse objects.
     * The API returns content as:
     *   para > [verse-tag + text-node, verse-tag + text-node, ...]
     */
    private fun extractVerses(content: ChapterContentDto): List<VerseDisplay> {
        val verses = mutableListOf<VerseDisplay>()
        var currentVerseNumber = 0
        var currentVerseId = ""

        fun walkItems(items: List<ContentItemDto>) {
            for (item in items) {
                if (item.type == "tag" && item.name == "verse") {
                    currentVerseNumber = item.attrs?.get("number")?.toIntOrNull() ?: currentVerseNumber
                    currentVerseId = item.attrs?.get("sid")?.replace(" ", ".") ?: ""
                    // recurse into verse items (usually just the number text, skip it)
                } else if (item.type == "text" && item.text != null) {
                    val verseId = item.attrs?.get("verseId") ?: ""
                    val text = item.text!!.trim()
                    if (verseId.isNotEmpty() && text.isNotEmpty() && currentVerseNumber > 0) {
                        // Check if we already have this verse (append if same verse continues)
                        val existing = verses.lastOrNull { it.verseId == verseId }
                        if (existing != null) {
                            val idx = verses.lastIndexOf(existing)
                            verses[idx] = existing.copy(text = existing.text + " " + text)
                        } else {
                            verses.add(
                                VerseDisplay(
                                    verseId = verseId,
                                    number = currentVerseNumber,
                                    text = text,
                                    chapterId = content.id,
                                    bookId = content.bookId,
                                )
                            )
                        }
                    }
                }
                item.items?.let { walkItems(it) }
            }
        }

        walkItems(content.content)
        return verses.sortedBy { it.number }
    }

    companion object {
        private const val TAG = "BibleRepo"
    }
}
