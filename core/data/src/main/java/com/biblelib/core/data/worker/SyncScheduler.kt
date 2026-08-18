package com.biblelib.core.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
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

    private fun buildRequest(abbr: String): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraints)
            .setInputData(workDataOf(SyncWorker.KEY_BIBLE_ABBR to abbr))
            .addTag(SyncWorker.TAG)
            .addTag(abbr)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

    /**
     * Queues a single bible's download as its own independent unit of work — the "one
     * download engine per bible" building block. Used both for one-off retries and as
     * the building block for [scheduleSecondaryDownloads].
     */
    fun scheduleSecondaryDownload(context: Context, abbr: String) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName(abbr),
            ExistingWorkPolicy.REPLACE,
            buildRequest(abbr),
        )
    }

    /**
     * Queues each bible in [abbrs] as its own uniquely-named work request, so they run
     * concurrently rather than one after another — e.g. once the primary bible finishes,
     * the default two secondary bibles both start downloading (and notifying) at once.
     * WorkManager's own executor still bounds how many actually run in parallel at any
     * instant; anything beyond that simply starts as soon as a slot frees up.
     */
    fun scheduleSecondaryDownloads(context: Context, abbrs: List<String>) {
        if (abbrs.isEmpty()) return

        val workManager = WorkManager.getInstance(context)
        abbrs.forEach { abbr ->
            workManager.enqueueUniqueWork(
                uniqueWorkName(abbr),
                ExistingWorkPolicy.KEEP, // don't clobber a download already in flight
                buildRequest(abbr),
            )
        }
    }

    fun cancelDownload(context: Context, abbr: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(abbr)
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(SyncWorker.TAG)
    }

    private fun uniqueWorkName(abbr: String) = "${SyncWorker.WORK_NAME_PREFIX}single_$abbr"
}
