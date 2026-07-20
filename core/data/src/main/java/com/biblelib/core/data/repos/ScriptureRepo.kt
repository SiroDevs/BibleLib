package com.biblelib.core.data.repos

import com.biblelib.core.database.daos.ScriptureItemDao
import com.biblelib.core.database.daos.ScriptureListDao
import com.biblelib.core.database.model.ScriptureItemEntity
import com.biblelib.core.database.model.ScriptureListEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScriptureRepo @Inject constructor(
    private val listDao: ScriptureListDao,
    private val itemDao: ScriptureItemDao,
) {
    suspend fun saveList(items: List<ScriptureItemEntity>, name: String? = null): Long =
        withContext(Dispatchers.IO) {
            require(items.isNotEmpty()) { "Cannot save an empty scripture list" }
            val listName = name?.takeIf { it.isNotBlank() } ?: items.first().reference
            val listId = listDao.insert(ScriptureListEntity(name = listName))
            itemDao.insertAll(items.mapIndexed { index, item -> item.copy(listId = listId, sortOrder = index) })
            listId
        }

    suspend fun getAllLists(): List<ScriptureListEntity> =
        withContext(Dispatchers.IO) { listDao.getAll() }

    suspend fun getList(listId: Long): ScriptureListEntity? =
        withContext(Dispatchers.IO) { listDao.getById(listId) }

    suspend fun getItems(listId: Long): List<ScriptureItemEntity> =
        withContext(Dispatchers.IO) { itemDao.getForList(listId) }

    suspend fun getItemCount(listId: Long): Int =
        withContext(Dispatchers.IO) { itemDao.countForList(listId) }

    suspend fun renameList(listId: Long, name: String) =
        withContext(Dispatchers.IO) { listDao.rename(listId, name) }

    suspend fun deleteList(listId: Long) = withContext(Dispatchers.IO) {
        itemDao.deleteForList(listId)
        listDao.delete(listId)
    }
}
