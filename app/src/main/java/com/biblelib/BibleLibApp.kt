package com.biblelib

import android.app.Application
import androidx.work.Configuration
import com.biblelib.core.ui.components.review.ReviewPromptManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BibleLibApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerConfiguration: Configuration

    override val workManagerConfiguration: Configuration
        get() = workerConfiguration

    override fun onCreate() {
        super.onCreate()
        ReviewPromptManager.recordFirstLaunch(this)
    }
}