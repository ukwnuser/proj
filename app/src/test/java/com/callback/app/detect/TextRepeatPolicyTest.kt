package com.callback.app.detect

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class TextRepeatPolicyTest {
    private val now = 1_700_000_000_000L

    @Test
    fun mayAutoText_whenNeverTexted() {
        assertTrue(TextRepeatPolicy.mayAutoText(lastTextedAtEpochMs = null, nowEpochMs = now))
    }

    @Test
    fun mayAutoText_after24Hours() {
        val last = now - TimeUnit.HOURS.toMillis(24)
        assertTrue(TextRepeatPolicy.mayAutoText(lastTextedAtEpochMs = last, nowEpochMs = now))
    }

    @Test
    fun mayNotAutoText_within24Hours() {
        val last = now - TimeUnit.HOURS.toMillis(23)
        assertFalse(TextRepeatPolicy.mayAutoText(lastTextedAtEpochMs = last, nowEpochMs = now))
    }

    @Test
    fun missedCallChecker_delegatesToPolicy() {
        val checker = MissedCallChecker()
        val recent = com.callback.app.data.TextedNumber(
            phoneNumber = "+15551234567",
            lastTextedAtEpochMs = now - TimeUnit.HOURS.toMillis(1),
        )
        assertFalse(checker.shouldAutoText(recent, now))
    }
}
