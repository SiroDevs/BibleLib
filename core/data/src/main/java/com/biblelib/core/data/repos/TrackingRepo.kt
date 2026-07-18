package com.biblelib.core.data.repos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.biblelib.core.database.daos.HistoryDao
import com.biblelib.core.database.daos.SearchDao
import com.biblelib.core.database.model.HistoryEntity
import com.biblelib.core.database.model.SearchEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingRepo @Inject constructor(
    private val historyDao: HistoryDao,
    private val searchDao: SearchDao,
) {
    private val dayFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

    /**
     * Records that [entry]'s chapter is being read. If this chapter was already opened today,
     * only the top-visible verse (and any refreshed labels) are updated — the original "first
     * opened" timestamp is preserved. Otherwise a new history row is created.
     */
    suspend fun recordReading(entry: HistoryEntity) = withContext(Dispatchers.IO) {
        val dayKey = dayFormat.format(Date(entry.readAt))
        val existing = historyDao.findForDay(entry.bibleAbbr, entry.chapterId, dayKey)
        if (existing != null) {
            historyDao.update(
                existing.copy(
                    bibleName = entry.bibleName,
                    bookName = entry.bookName,
                    chapterRef = entry.chapterRef,
                    verseNumber = entry.verseNumber ?: existing.verseNumber,
                )
            )
        } else {
            historyDao.insert(entry.copy(dayKey = dayKey))
            historyDao.pruneOld()
        }
    }

    suspend fun getReadingHistory(): List<HistoryEntity> =
        withContext(Dispatchers.IO) { historyDao.getRecent() }

    suspend fun clearHistory() = withContext(Dispatchers.IO) { historyDao.deleteAll() }

    suspend fun recordSearch(query: String) = withContext(Dispatchers.IO) {
        searchDao.insert(SearchEntity(query = query))
        searchDao.pruneOld()
    }

    suspend fun getSearchHistory(): List<SearchEntity> =
        withContext(Dispatchers.IO) { searchDao.getRecent() }

    suspend fun clearSearchHistory() = withContext(Dispatchers.IO) { searchDao.deleteAll() }
}
