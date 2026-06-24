package com.biblelib.core.data.di

import android.content.Context
import com.biblelib.core.data.repos.DraftRepo
import com.biblelib.core.data.repos.EditorRepo
import com.biblelib.core.data.repos.ListingRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ReportRepo
import com.biblelib.core.data.repos.SongBookRepo
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.data.repos.TrackingRepo
import com.biblelib.core.data.repos.UserRepo
import com.biblelib.core.database.daos.BookDao
import com.biblelib.core.database.daos.DraftDao
import com.biblelib.core.database.daos.EditDao
import com.biblelib.core.database.daos.HistoryDao
import com.biblelib.core.database.daos.ListingDao
import com.biblelib.core.database.daos.SearchDao
import com.biblelib.core.database.daos.SongDao
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
    fun provideSongBookRepo(
        apiService: BibleLibService,
        booksDao: BookDao,
        songsDao: SongDao,
    ): SongBookRepo = SongBookRepo(apiService, booksDao, songsDao)

    @Provides @Singleton
    fun provideListingRepo(listingsDao: ListingDao): ListingRepo =
        ListingRepo(listingsDao)

    @Provides @Singleton
    fun provideTrackingRepo(
        historiesDao: HistoryDao,
        searchesDao: SearchDao
    ): TrackingRepo = TrackingRepo(historiesDao, searchesDao)

    @Provides @Singleton
    fun provideReportRepo(service: BibleLibService): ReportRepo =
        ReportRepo(service)

    @Provides @Singleton
    fun provideDraftRepo(draftsDao: DraftDao, service: BibleLibService): DraftRepo =
        DraftRepo(draftsDao, service)

    @Provides @Singleton
    fun provideEditRepo(editDao: EditDao, service: BibleLibService): EditorRepo =
        EditorRepo(editDao, service)

    @Provides @Singleton
    fun provideUserRepo(service: BibleLibService, prefsRepo: PrefsRepo): UserRepo =
        UserRepo(service, prefsRepo)
}
