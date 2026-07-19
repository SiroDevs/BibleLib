package com.biblelib.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A named collection of scriptures built via the Scripture Opener, e.g. for a sermon
 * or a devotional reading plan. [name] defaults to the reference of the first scripture
 * added, but can be renamed by the user.
 */
@Entity(tableName = "scripture_lists")
data class ScriptureListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
)
