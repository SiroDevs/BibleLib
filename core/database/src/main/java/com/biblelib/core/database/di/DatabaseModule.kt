package com.biblelib.core.database.di

import android.content.Context
import androidx.room.Room
import com.biblelib.core.database.AppDatabase
import com.biblelib.core.database.daos.BookDao
import com.biblelib.core.database.daos.DraftDao
import com.biblelib.core.database.daos.EditDao
import com.biblelib.core.database.daos.HistoryDao
import com.biblelib.core.database.daos.ListingDao
import com.biblelib.core.database.daos.SearchDao
import com.biblelib.core.database.daos.SongDao
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

    @Provides fun provideBookDao(db: AppDatabase): BookDao = db.booksDao()
    @Provides fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historiesDao()
    @Provides fun provideListingDao(db: AppDatabase): ListingDao = db.listingsDao()
    @Provides fun provideSearchDao(db: AppDatabase): SearchDao = db.searchesDao()
    @Provides fun provideSongDao(db: AppDatabase): SongDao = db.songsDao()
    @Provides fun provideDraftDao(db: AppDatabase): DraftDao = db.draftsDao()
    @Provides fun provideEditDao(db: AppDatabase): EditDao = db.editsDao()
}
