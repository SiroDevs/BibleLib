package com.biblelib.core.data.repos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.biblelib.core.database.daos.HistoryDao
import com.biblelib.core.database.daos.SearchDao
import com.biblelib.core.database.model.HistoryEntity
import com.biblelib.core.database.model.SearchEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingRepo @Inject constructor(
    private val historyDao: HistoryDao,
    private val searchDao: SearchDao,
) {
    suspend fun recordReading(entry: HistoryEntity) = withContext(Dispatchers.IO) {
        historyDao.insert(
            HistoryEntity(
                bibleAbbr = entry.bibleAbbr,
                bookId = entry.bookId,
                bookName = entry.bookName,
                chapterId = entry.chapterId,
                chapterRef = entry.chapterRef,
                readAt = entry.readAt,
            )
        )
        historyDao.pruneOld()
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
