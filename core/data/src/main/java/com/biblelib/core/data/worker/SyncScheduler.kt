package com.biblelib.core.data.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncScheduler {
    /** Constraints: needs any network connection. Battery / charging agnostic. */
    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /**
     * Schedules the post-selection sync (first install or re-selection).
     * Uses [ExistingWorkPolicy.REPLACE] so that if the user re-selects Bibles the
     * old sync is cancelled and a fresh one starts with the updated selection.
     */
    fun scheduleInstallSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorkerFactory>()
            .setConstraints(networkConstraints)
            .addTag(SyncWorkerFactory.TAG)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncWorkerFactory.INSTALL_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,   // replace on re-selection so new Bibles are picked up
            request,
        )
    }

    /**
     * Schedules the daily background re-sync.
     * Uses [ExistingWorkPolicy.KEEP] so if the user opens the app twice within
     * a short window the second call does nothing.
     */
    fun scheduleDailySync(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorkerFactory>()
            .setConstraints(networkConstraints)
            .addTag(SyncWorkerFactory.TAG)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncWorkerFactory.DAILY_SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }
}