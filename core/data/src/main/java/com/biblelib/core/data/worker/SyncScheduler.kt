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

    fun scheduleSecondaryDownload(context: Context, abbr: String) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            "${SyncWorker.WORK_NAME_PREFIX}single_$abbr",
            ExistingWorkPolicy.REPLACE,
            buildRequest(abbr),
        )
    }

    fun scheduleSecondaryDownloads(context: Context, abbrs: List<String>) {
        if (abbrs.isEmpty()) return

        val workManager = WorkManager.getInstance(context)
        val requests = abbrs.map { buildRequest(it) }

        var continuation = workManager.beginUniqueWork(
            "${SyncWorker.WORK_NAME_PREFIX}queue",
            ExistingWorkPolicy.REPLACE,
            requests.first(),
        )
        requests.drop(1).forEach { continuation = continuation.then(it) }
        continuation.enqueue()
    }

    fun cancelDownload(context: Context, abbr: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(abbr)
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(SyncWorker.TAG)
    }
}
