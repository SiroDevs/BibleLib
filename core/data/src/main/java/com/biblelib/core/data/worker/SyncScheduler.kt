package com.biblelib.core.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object SyncScheduler {

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /**
     * Schedule download for a secondary bible (runs in foreground with notification).
     * Uses REPLACE so a re-selection always restarts cleanly.
     */
    fun scheduleSecondaryDownload(context: Context, abbr: String) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraints)
            .setInputData(workDataOf(SyncWorker.KEY_BIBLE_ABBR to abbr))
            .addTag(SyncWorker.TAG)
            .addTag(abbr)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "${SyncWorker.WORK_NAME_PREFIX}$abbr",
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    /** Cancel a specific bible download. */
    fun cancelDownload(context: Context, abbr: String) {
        WorkManager.getInstance(context)
            .cancelUniqueWork("${SyncWorker.WORK_NAME_PREFIX}$abbr")
    }

    /** Cancel all pending bible downloads. */
    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(SyncWorker.TAG)
    }
}
