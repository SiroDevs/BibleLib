package com.biblelib.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration1To2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `bibles_new` (
                `abbreviation` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `languageName` TEXT NOT NULL,
                `scriptDirection` TEXT NOT NULL,
                `sortOrder` INTEGER NOT NULL,
                `isDownloaded` INTEGER NOT NULL,
                `addedAt` INTEGER NOT NULL,
                `countryName` TEXT NOT NULL,
                `downloadProgress` REAL NOT NULL,
                `downloadFailed` INTEGER NOT NULL,
                PRIMARY KEY(`abbreviation`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `bibles_new` (
                `abbreviation`, `name`, `description`, `languageName`, `scriptDirection`,
                `sortOrder`, `isDownloaded`, `addedAt`, `countryName`, `downloadProgress`, `downloadFailed`
            )
            SELECT
                `abbreviation`, `name`, `description`, `languageName`, `scriptDirection`,
                `sortOrder`, `isDownloaded`, `addedAt`, `countryName`, `downloadProgress`, `downloadFailed`
            FROM `bibles`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `bibles`")
        db.execSQL("ALTER TABLE `bibles_new` RENAME TO `bibles`")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `verses_new` (
                `chapterId` TEXT NOT NULL,
                `bibleAbbr` TEXT NOT NULL,
                `bookId` TEXT NOT NULL,
                `verseCount` INTEGER NOT NULL,
                `contentJson` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`chapterId`, `bibleAbbr`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `verses_new` (
                `chapterId`, `bibleAbbr`, `bookId`, `verseCount`, `contentJson`, `cachedAt`
            )
            SELECT
                `chapterId`, `bibleAbbr`, `bookId`, `verseCount`, `contentJson`, `cachedAt`
            FROM `verses`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `verses`")
        db.execSQL("ALTER TABLE `verses_new` RENAME TO `verses`")

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_verses_bibleAbbr` ON `verses` (`bibleAbbr`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_verses_bookId` ON `verses` (`bookId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_verses_chapterId` ON `verses` (`chapterId`)")
    }
}
