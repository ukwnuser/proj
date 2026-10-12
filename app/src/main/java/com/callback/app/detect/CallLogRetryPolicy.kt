package com.callback.app.detect

/** Bounded retries while waiting for a MISSED call-log row to appear. */
object CallLogRetryPolicy {
    const val MAX_ATTEMPTS: Int = 5
    const val INITIAL_DELAY_SECONDS: Long = 4L
    const val BACKOFF_DELAY_SECONDS: Long = 5L

    fun shouldRetry(runAttemptCount: Int, callLogFound: Boolean, maxAttempts: Int = MAX_ATTEMPTS): Boolean {
        if (callLogFound) return false
        return runAttemptCount < maxAttempts - 1
    }
}
