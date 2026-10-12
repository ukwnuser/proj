package com.callback.app.detect

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallLogRetryPolicyTest {
    @Test
    fun shouldRetry_whenNotFoundUntilMaxAttempts() {
        assertTrue(CallLogRetryPolicy.shouldRetry(runAttemptCount = 0, callLogFound = false))
        assertTrue(CallLogRetryPolicy.shouldRetry(runAttemptCount = 3, callLogFound = false))
        assertFalse(CallLogRetryPolicy.shouldRetry(runAttemptCount = 4, callLogFound = false))
    }

    @Test
    fun shouldNotRetry_whenCallLogFound() {
        assertFalse(CallLogRetryPolicy.shouldRetry(runAttemptCount = 0, callLogFound = true))
    }
}
