package com.biblelib.core.data.worker

import android.Manifest
import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object DownloadNotifier {

    const val CHANNEL_ID = "bible_downloads"
    const val ACTION_RETRY = "com.biblelib.core.data.action.RETRY_DOWNLOAD"
    const val ACTION_RESTART = "com.biblelib.core.data.action.RESTART_DOWNLOAD"
    const val ACTION_DISMISS = "com.biblelib.core.data.action.DISMISS_DOWNLOAD"
    const val EXTRA_ABBR = "extra_bible_abbr"

    private const val ID_BASE = 8_000
    private const val ID_RANGE = 0x0FFF // keeps ids in a small, predictable band above ID_BASE

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
        val percent = (progress.coerceIn(0f, 1f) * 100).toInt()
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading $abbr bible — $percent%")
            .setContentText(step)
            .setProgress(100, percent, progress <= 0f)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showFailed(context: Context, abbr: String) {
        if (!hasPermission(context)) return
        ensureChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.stat_sys_warning)
            .setContentTitle("$abbr bible download failed")
            .setContentText("Tap Retry to try again, or Restart to download it from scratch.")
            .setOnlyAlertOnce(true)
            .setOngoing(false)
            .setAutoCancel(true)
            .setDeleteIntent(actionIntent(context, ACTION_DISMISS, abbr, requestCodeOffset = 3))
            .addAction(
                R.drawable.ic_popup_sync,
                "RETRY",
                actionIntent(context, ACTION_RETRY, abbr, requestCodeOffset = 1),
            )
            .addAction(
                R.drawable.ic_menu_revert,
                "RESTART",
                actionIntent(context, ACTION_RESTART, abbr, requestCodeOffset = 2),
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationIdFor(abbr), notification)
        } catch (e: SecurityException) {
        }
    }

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
            notificationIdFor(abbr) * 10 + requestCodeOffset,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
