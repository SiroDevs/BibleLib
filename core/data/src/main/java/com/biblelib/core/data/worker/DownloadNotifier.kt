package com.biblelib.core.data.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Owns notification presentation for concurrent, per-bible downloads.
 *
 * Each bible gets its own stable notification id (derived from its abbreviation), so
 * downloading several bibles at once shows several independent progress notifications
 * instead of one worker stomping on another's.
 *
 * - While downloading: an ongoing, silent progress notification (see [progressNotification],
 *   used as the worker's foreground notification so it survives the app being minimised).
 * - On success: the notification is removed ([cancel]) — nothing lingers for a completed download.
 * - On permanent failure: an actionable notification with Retry / Restart is left behind
 *   ([showFailed]); it's the only state that persists in the notification shade.
 */
object DownloadNotifier {

    const val CHANNEL_ID = "bible_downloads"
    const val ACTION_RETRY = "com.biblelib.core.data.action.RETRY_DOWNLOAD"
    const val ACTION_RESTART = "com.biblelib.core.data.action.RESTART_DOWNLOAD"
    const val ACTION_DISMISS = "com.biblelib.core.data.action.DISMISS_DOWNLOAD"
    const val EXTRA_ABBR = "extra_bible_abbr"

    private const val ID_BASE = 8_000
    private const val ID_RANGE = 0x0FFF // keeps ids in a small, predictable band above ID_BASE

    /** Stable per-bible notification id, so concurrent downloads never collide. */
    fun notificationIdFor(abbr: String): Int =
        ID_BASE + (abbr.uppercase().hashCode() and ID_RANGE)

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Bible downloads", NotificationManager.IMPORTANCE_LOW)
                    .apply { description = "Progress and status for bible downloads" }
            )
        }
    }

    fun progressNotification(
        context: Context,
        abbr: String,
        progress: Float,
        step: String,
    ): Notification {
        ensureChannel(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading ${abbr.uppercase()} Bible")
            .setContentText(step)
            .setProgress(100, (progress * 100).toInt(), progress <= 0f)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    fun showFailed(context: Context, abbr: String) {
        ensureChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("${abbr.uppercase()} Bible download failed")
            .setContentText("Tap Retry to try again, or Restart to download it from scratch.")
            .setOnlyAlertOnce(true)
            .setOngoing(false)
            .setAutoCancel(true)
            .setDeleteIntent(actionIntent(context, ACTION_DISMISS, abbr, requestCodeOffset = 3))
            .addAction(
                android.R.drawable.ic_popup_sync,
                "Retry",
                actionIntent(context, ACTION_RETRY, abbr, requestCodeOffset = 1),
            )
            .addAction(
                android.R.drawable.ic_menu_revert,
                "Restart",
                actionIntent(context, ACTION_RESTART, abbr, requestCodeOffset = 2),
            )
            .build()

        NotificationManagerCompat.from(context).notify(notificationIdFor(abbr), notification)
    }

    /** Removes any notification for [abbr] — call on success, or once an action has been handled. */
    fun cancel(context: Context, abbr: String) {
        NotificationManagerCompat.from(context).cancel(notificationIdFor(abbr))
    }

    private fun actionIntent(
        context: Context,
        action: String,
        abbr: String,
        requestCodeOffset: Int,
    ): PendingIntent {
        val intent = Intent(context, DownloadActionReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_ABBR, abbr)
        }
        return PendingIntent.getBroadcast(
            context,
            notificationIdFor(abbr) * 10 + requestCodeOffset, // unique per abbr+action
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
