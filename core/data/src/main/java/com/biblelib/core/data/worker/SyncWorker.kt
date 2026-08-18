package com.biblelib.core.data.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.biblelib.core.common.helpers.NetworkUtils
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.network.util.RetryPolicy

/**
 * Downloads a single bible. Each bible gets its own [SyncWorker] instance (see
 * [SyncScheduler]), each running as its own foreground job with its own notification
 * ([DownloadNotifier]) — that's what lets several bibles download concurrently, each
 * visibly, even once the app is minimised.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val bibleRepo: BibleRepo,
    private val prefsRepo: PrefsRepo,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.w(TAG, "No network – retrying later")
            return Result.retry()
        }

        val abbr = inputData.getString(KEY_BIBLE_ABBR) ?: run {
            Log.e(TAG, "No bible abbreviation provided")
            return Result.failure()
        }

        return try {
            Log.d(TAG, "▶ Downloading secondary bible: $abbr")
            setForeground(createForegroundInfo(abbr, 0f))

            bibleRepo.downloadBible(abbr) { step, progress ->
                Log.d(TAG, "[$abbr] $step (${"%.0f".format(progress * 100)}%)")
                setForeground(createForegroundInfo(abbr, progress, step))
                setProgress(workDataOf(KEY_PROGRESS to progress, KEY_STEP to step))
            }

            prefsRepo.lastSyncedAt = System.currentTimeMillis()
            DownloadNotifier.cancel(context, abbr)
            Log.d(TAG, "✅ Secondary bible $abbr downloaded")
            Result.success()
        } catch (e: Exception) {
            val failure = RetryPolicy.classify(e)
            Log.e(TAG, "❌ Failed to download $abbr: ${failure.message}", failure)

            // 404 / 401 / 403 are permanent — don't burn through WorkManager's retry
            // budget on something that will never succeed on its own.
            val canRetry = !failure.isPermanent && runAttemptCount < MAX_RETRIES
            if (canRetry) {
                Result.retry()
            } else {
                bibleRepo.markDownloadFailed(abbr)
                DownloadNotifier.showFailed(context, abbr)
                Result.failure()
            }
        }
    }

    private fun createForegroundInfo(
        abbr: String,
        progress: Float,
        step: String = "Downloading...",
    ): ForegroundInfo {
        val notification = DownloadNotifier.progressNotification(context, abbr, progress, step)
        val notificationId = DownloadNotifier.notificationIdFor(abbr)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    companion object {
        const val TAG = "SyncWorker"
        const val WORK_NAME_PREFIX = "bible_download_"
        const val KEY_BIBLE_ABBR = "bible_abbr"
        const val KEY_PROGRESS = "progress"
        const val KEY_STEP = "step"

        private const val MAX_RETRIES = 3
    }
}
