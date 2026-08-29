package com.biblelib.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration2To3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `searches_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `qry` TEXT NOT NULL,
                `queriedAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `searches_new` (`id`, `qry`, `queriedAt`)
            SELECT `id`, `query`, `searchedAt`
            FROM `searches`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `searches`")
        db.execSQL("ALTER TABLE `searches_new` RENAME TO `searches`")
    }
}
