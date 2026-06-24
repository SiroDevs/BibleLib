package com.biblelib.app.di

import com.biblelib.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {
    @Provides
    @Named("paystack_secret_key")
    fun providePaystackSecretKey(): String = BuildConfig.PaystackSecretKey
}