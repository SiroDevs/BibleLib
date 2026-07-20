package com.biblelib.core.database.di

import android.content.Context
import androidx.room.Room
import com.biblelib.core.database.AppDatabase
import com.biblelib.core.database.daos.BibleDao
import com.biblelib.core.database.daos.BookDao
import com.biblelib.core.database.daos.BookmarkDao
import com.biblelib.core.database.daos.ChapterDao
import com.biblelib.core.database.daos.NoteDao
import com.biblelib.core.database.daos.ScriptureItemDao
import com.biblelib.core.database.daos.ScriptureListDao
import com.biblelib.core.database.daos.VerseDao
import com.biblelib.core.database.daos.HistoryDao
import com.biblelib.core.database.daos.SearchDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase =
        Room.databaseBuilder(appContext, AppDatabase::class.java, "BibleLib")
            .build()

    @Provides fun provideBibleDao(db: AppDatabase): BibleDao = db.biblesDao()
    @Provides fun provideDraftDao(db: AppDatabase): BookDao = db.booksDao()
    @Provides fun provideChapterDao(db: AppDatabase): ChapterDao = db.chaptersDao()
    @Provides fun provideVerseDao(db: AppDatabase): VerseDao = db.versesDao()
    @Provides fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historiesDao()
    @Provides fun provideSearchDao(db: AppDatabase): SearchDao = db.searchesDao()
    @Provides fun provideBookmarkDao(db: AppDatabase): BookmarkDao = db.bookmarksDao()
    @Provides fun provideNoteDao(db: AppDatabase): NoteDao = db.notesDao()
    @Provides fun provideScriptureListDao(db: AppDatabase): ScriptureListDao = db.scriptureListsDao()
    @Provides fun provideScriptureItemDao(db: AppDatabase): ScriptureItemDao = db.scriptureItemsDao()
}