package com.biblelib.core.database.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = "bibles")
data class BibleEntity(
    @PrimaryKey
    val abbreviation: String,
    val name: String,
    val description: String,
    val languageName: String,
    val scriptDirection: String,
    val copyright: String,
    val sortOrder: Int = 0,
    val isDownloaded: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    val countryName: String = "",
    val downloadProgress: Float = 0f,
    val downloadFailed: Boolean = false,
) : Parcelable
