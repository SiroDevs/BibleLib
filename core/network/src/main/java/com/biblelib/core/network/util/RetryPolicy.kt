package com.biblelib.core.network.util

import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.min

/**
 * Classified outcome of a failed network call, so callers (workers, UI) can decide
 * whether to retry, surface a permanent failure, or prompt for re-authentication —
 * without re-parsing HTTP codes everywhere.
 */
sealed class DownloadFailure(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** 500 / 502 / 503 / other 5xx. Transient — safe to retry with backoff. */
    class ServerError(val code: Int, cause: Throwable? = null) :
        DownloadFailure("Server error $code", cause)

    /** Connect/read timeouts, DNS failures, dropped connections. Safe to retry. */
    class Network(cause: Throwable? = null) : DownloadFailure("Network error", cause)

    /** 429 — must back off for at least [retryAfterMillis] before retrying. */
    class RateLimited(val retryAfterMillis: Long, cause: Throwable? = null) :
        DownloadFailure("Rate limited", cause)

    /** 404 — the resource doesn't exist. Retrying won't help; fail fast. */
    class NotFound(cause: Throwable? = null) : DownloadFailure("Not found", cause)

    /** 401 / 403 — needs re-authentication. Not retryable on its own. */
    class Unauthorized(cause: Throwable? = null) : DownloadFailure("Unauthorized", cause)

    /** Anything else — treated conservatively as retryable a limited number of times. */
    class Unknown(cause: Throwable? = null) : DownloadFailure(cause?.message ?: "Unknown error", cause)

    /** True for failures where retrying is pointless (bad request, needs auth, gone). */
    val isPermanent: Boolean
        get() = this is NotFound || this is Unauthorized
}

/**
 * Central place implementing:
 *
 * 500 / 502 / 503 / timeout -> retry (exponential backoff)
 * 429                       -> respect Retry-After
 * 404                       -> don't retry indefinitely (fail fast)
 * 401 / 403                 -> fail (needs auth refresh)
 */
object RetryPolicy {

    fun classify(throwable: Throwable): DownloadFailure = when (throwable) {
        is DownloadFailure -> throwable
        is HttpException -> when (throwable.code()) {
            404 -> DownloadFailure.NotFound(throwable)
            401, 403 -> DownloadFailure.Unauthorized(throwable)
            429 -> DownloadFailure.RateLimited(retryAfterMillis(throwable), throwable)
            in 500..599 -> DownloadFailure.ServerError(throwable.code(), throwable)
            else -> DownloadFailure.Unknown(throwable)
        }
        is IOException -> DownloadFailure.Network(throwable) // covers SocketTimeoutException too
        else -> DownloadFailure.Unknown(throwable)
    }

    /**
     * Runs [block], retrying on transient failures with exponential backoff.
     * Throws the classified [DownloadFailure] once attempts are exhausted or the
     * failure is permanent (404 / 401 / 403).
     */
    suspend fun <T> retrying(
        maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
        initialDelayMillis: Long = DEFAULT_INITIAL_DELAY_MS,
        maxDelayMillis: Long = DEFAULT_MAX_DELAY_MS,
        block: suspend () -> T,
    ): T {
        var attempt = 0
        var backoffMs = initialDelayMillis

        while (true) {
            attempt++
            try {
                return block()
            } catch (t: Throwable) {
                val failure = classify(t)

                if (failure.isPermanent || attempt >= maxAttempts) throw failure

                val waitMs = when (failure) {
                    is DownloadFailure.RateLimited -> failure.retryAfterMillis
                    else -> backoffMs.also { backoffMs = min(backoffMs * 2, maxDelayMillis) }
                }
                delay(waitMs)
            }
        }
    }

    private fun retryAfterMillis(e: HttpException): Long {
        val header = e.response()?.headers()?.get("Retry-After")
        val seconds = header?.toLongOrNull()
        return (seconds ?: DEFAULT_RATE_LIMIT_WAIT_SECONDS) * 1000
    }

    private const val DEFAULT_MAX_ATTEMPTS = 4
    private const val DEFAULT_INITIAL_DELAY_MS = 1_000L
    private const val DEFAULT_MAX_DELAY_MS = 20_000L
    private const val DEFAULT_RATE_LIMIT_WAIT_SECONDS = 5L
}
