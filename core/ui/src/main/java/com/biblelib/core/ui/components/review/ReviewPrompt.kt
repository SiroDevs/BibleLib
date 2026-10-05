package com.biblelib.core.ui.components.review

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

data class ReviewPromptConfig(
    val initialDelay: Duration = 2.days,
    val reminderDelay: Duration = 2.days,
    val showAfter: Duration = 3.seconds,
    val prefsName: String = "review_prompt_prefs",
)

private object ReviewPrefKeys {
    const val FIRST_LAUNCH_AT = "review_first_launch_at"
    const val LATER_AT = "review_later_at"
    const val HANDLED = "review_handled"
}

class ReviewPromptManager(
    context: Context,
    private val config: ReviewPromptConfig = ReviewPromptConfig(),
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val prefs = context.applicationContext
        .getSharedPreferences(config.prefsName, Context.MODE_PRIVATE)

    init {
        if (prefs.getLong(ReviewPrefKeys.FIRST_LAUNCH_AT, 0L) == 0L) {
            prefs.edit { putLong(ReviewPrefKeys.FIRST_LAUNCH_AT, now()) }
        }
    }

    fun shouldPrompt(): Boolean {
        if (prefs.getBoolean(ReviewPrefKeys.HANDLED, false)) return false

        val current = now()
        val firstLaunch = prefs.getLong(ReviewPrefKeys.FIRST_LAUNCH_AT, current)
        if (current - firstLaunch < config.initialDelay.inWholeMilliseconds) return false

        val laterAt = prefs.getLong(ReviewPrefKeys.LATER_AT, 0L)
        if (laterAt != 0L && current - laterAt < config.reminderDelay.inWholeMilliseconds) return false

        return true
    }

    fun markLater() = prefs.edit { putLong(ReviewPrefKeys.LATER_AT, now()) }

    fun markHandled() = prefs.edit { putBoolean(ReviewPrefKeys.HANDLED, true) }

    suspend fun launchReview(activity: Activity) {
        val flowStarted = try {
            val manager = ReviewManagerFactory.create(activity)
            val info = manager.requestReview()
            markHandled()
            manager.launchReview(activity, info)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }

        if (!flowStarted) {
            if (activity.openStoreListing()) markHandled() else markLater()
        }
    }

    companion object {
        fun recordFirstLaunch(context: Context, config: ReviewPromptConfig = ReviewPromptConfig()) {
            ReviewPromptManager(context, config)
        }
    }
}

private enum class ReviewStep { None, Enjoying, Rate }

private var shownThisSession = false

@Composable
fun ReviewPromptHost(
    config: ReviewPromptConfig = ReviewPromptConfig(),
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    val manager = remember(config) { ReviewPromptManager(context, config) }
    val scope = rememberCoroutineScope()
    var step by rememberSaveable { mutableStateOf(ReviewStep.None) }

    LaunchedEffect(manager, enabled) {
        if (!enabled || step != ReviewStep.None || shownThisSession) return@LaunchedEffect
        if (!manager.shouldPrompt()) return@LaunchedEffect
        delay(config.showAfter)
        if (!shownThisSession) {
            shownThisSession = true
            step = ReviewStep.Enjoying
        }
    }

    fun later() {
        manager.markLater()
        step = ReviewStep.None
    }

    when (step) {
        ReviewStep.None -> Unit

        ReviewStep.Enjoying -> AlertDialog(
            onDismissRequest = ::later,
            title = { Text("Are you enjoying the app?") },
            confirmButton = { TextButton(onClick = { step = ReviewStep.Rate }) { Text("Yes") } },
            dismissButton = { TextButton(onClick = { step = ReviewStep.Rate }) { Text("No") } },
        )

        ReviewStep.Rate -> AlertDialog(
            onDismissRequest = ::later,
            title = { Text("Leave a review?") },
            text = { Text("A quick rating helps other people find the app and helps us improve it.") },
            confirmButton = {
                TextButton(onClick = {
                    step = ReviewStep.None
                    val activity = context.findActivity()
                    if (activity == null) manager.markLater()
                    else scope.launch { manager.launchReview(activity) }
                }) { Text("Review Now") }
            },
            dismissButton = { TextButton(onClick = ::later) { Text("Later") } },
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun Activity.openStoreListing(): Boolean =
    listOf(
        "market://details?id=$packageName",
        "https://play.google.com/store/apps/details?id=$packageName",
    ).any { uri ->
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }