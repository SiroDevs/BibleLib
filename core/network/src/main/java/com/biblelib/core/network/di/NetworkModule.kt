package com.biblelib.core.network.di

import com.biblelib.core.network.services.PaystackService
import com.biblelib.core.network.services.BibleLibService
import com.biblelib.core.common.utils.ApiConstants
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named

@InstallIn(SingletonComponent::class)
@Module
@Suppress("unused")
object NetworkModule {
    @Provides
    @Reusable
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()

    @Provides
    @Reusable
    fun providePaystackApiService(@Named("paystackApi") retrofit: Retrofit): PaystackService {
        return retrofit.create(PaystackService::class.java)
    }

    @Provides
    @Named("paystackApi")
    @Reusable
    fun providePaystackApi(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConstants.PAYSTACK_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Reusable
    @Named("bibleLibApi")
    fun provideBibleLibRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(ApiConstants.BIBLELIB_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    @Provides
    @Reusable
    fun provideBibleLibService(@Named("bibleLibApi") retrofit: Retrofit): BibleLibService =
        retrofit.create(BibleLibService::class.java)
}
