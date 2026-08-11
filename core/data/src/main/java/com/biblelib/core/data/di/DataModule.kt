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
    // NOT part of the assets/selection-screen change. Found while debugging the blank bible
    // list: PrefsRepo, ThemeRepo, BibleRepo, and TrackingRepo each already declare their own
    // `@Inject constructor`, so Hilt can provide them on its own. The manual @Provides methods
    // below created a second, conflicting binding for the same four types, which Dagger/Hilt
    // rejects at compile time ([Dagger/DuplicateBindings]) - the module (and app) couldn't have
    // built with this in place. Commented out rather than deleted so the original wiring is
    // still visible; safe to leave disabled since the @Inject constructors cover the same deps.
    //
    // @Provides @Singleton
    // fun providePreferencesRepo(@ApplicationContext context: Context): PrefsRepo =
    //     PrefsRepo(context)
    //
    // @Provides @Singleton
    // fun provideThemeRepo(prefsRepo: PrefsRepo): ThemeRepo =
    //     ThemeRepo(prefsRepo)
    //
    // @Provides @Singleton
    // fun provideBibleRepo(
    //     service: BibleLibService,
    //     savedBibleDao: BibleDao,
    //     bookDao: BookDao,
    //     chapterDao: ChapterDao,
    //     verseDao: VerseDao,
    // ): BibleRepo = BibleRepo(service, savedBibleDao, bookDao, chapterDao, verseDao)
    //
    // @Provides @Singleton
    // fun provideTrackingRepo(
    //     historyDao: HistoryDao,
    //     searchDao: SearchDao,
    // ): TrackingRepo = TrackingRepo(historyDao, searchDao)
}
