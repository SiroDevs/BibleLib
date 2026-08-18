package com.biblelib.core.data.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.biblelib.core.data.repos.BibleRepo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles taps on the Retry / Restart / dismiss actions of a failed-download
 * notification (see [DownloadNotifier]). Mirrors the retry/restart behaviour already
 * exposed in the UI (BiblesViewModel, DownloadController) so a failed download can be
 * driven entirely from the notification shade, even with the app killed.
 */
@AndroidEntryPoint
class DownloadActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var bibleRepo: BibleRepo

    override fun onReceive(context: Context, intent: Intent) {
        val abbr = intent.getStringExtra(DownloadNotifier.EXTRA_ABBR) ?: return
        val appContext = context.applicationContext

        when (intent.action) {
            DownloadNotifier.ACTION_RETRY -> {
                DownloadNotifier.cancel(appContext, abbr)
                SyncScheduler.scheduleSecondaryDownload(appContext, abbr)
            }

            DownloadNotifier.ACTION_RESTART -> {
                DownloadNotifier.cancel(appContext, abbr)
                val pendingResult = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        SyncScheduler.cancelDownload(appContext, abbr)
                        bibleRepo.clearBibleContent(abbr)
                        SyncScheduler.scheduleSecondaryDownload(appContext, abbr)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to restart download for $abbr", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            DownloadNotifier.ACTION_DISMISS -> DownloadNotifier.cancel(appContext, abbr)
        }
    }

    companion object {
        private const val TAG = "DownloadActionReceiver"
    }
}
