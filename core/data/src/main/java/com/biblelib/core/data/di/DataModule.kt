package com.biblelib.core.data.di

import android.content.Context
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.data.repos.TrackingRepo
import com.biblelib.core.database.daos.BibleDao
import com.biblelib.core.database.daos.BookDao
import com.biblelib.core.database.daos.ChapterDao
import com.biblelib.core.database.daos.VerseDao
import com.biblelib.core.database.daos.HistoryDao
import com.biblelib.core.database.daos.SearchDao
import com.biblelib.core.network.di.NetworkModule
import com.biblelib.core.network.services.BibleLibService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module(includes = [NetworkModule::class])
object DataModule {
    @Provides @Singleton
    fun providePreferencesRepo(@ApplicationContext context: Context): PrefsRepo =
        PrefsRepo(context)

    @Provides @Singleton
    fun provideThemeRepo(prefsRepo: PrefsRepo): ThemeRepo =
        ThemeRepo(prefsRepo)

    @Provides @Singleton
    fun provideBibleRepo(
        service: BibleLibService,
        savedBibleDao: BibleDao,
        bookDao: BookDao,
        chapterDao: ChapterDao,
        verseDao: VerseDao,
    ): BibleRepo = BibleRepo(service, savedBibleDao, bookDao, chapterDao, verseDao)

    @Provides @Singleton
    fun provideTrackingRepo(
        historyDao: HistoryDao,
        searchDao: SearchDao,
    ): TrackingRepo = TrackingRepo(historyDao, searchDao)
}
