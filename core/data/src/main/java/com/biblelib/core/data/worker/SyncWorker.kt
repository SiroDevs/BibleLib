package com.biblelib.core.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
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
            Log.d(TAG, "✅ Secondary bible $abbr downloaded")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to download $abbr: ${e.message}", e)
            Result.retry()
        }
    }

    private fun createForegroundInfo(
        abbr: String,
        progress: Float,
        step: String = "Downloading…"
    ): ForegroundInfo {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Bible Downloads", NotificationManager.IMPORTANCE_LOW)
                    .apply { description = "Background bible download progress" }
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading ${abbr.uppercase()} Bible")
            .setContentText(step)
            .setProgress(100, (progress * 100).toInt(), progress == 0f)
            .setOngoing(true)
            .setSilent(true)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    companion object {
        const val TAG                  = "SyncWorker"
        const val WORK_NAME_PREFIX     = "bible_download_"
        const val KEY_BIBLE_ABBR       = "bible_abbr"
        const val KEY_PROGRESS         = "progress"
        const val KEY_STEP             = "step"
        private const val CHANNEL_ID   = "bible_downloads"
        private const val NOTIFICATION_ID = 8001
    }
}
