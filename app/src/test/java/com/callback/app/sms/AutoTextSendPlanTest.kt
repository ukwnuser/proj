package com.callback.app.sms

import com.callback.app.data.TextedNumber
import com.callback.app.detect.MissedCallChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.concurrent.TimeUnit

class AutoTextSendPlanTest {
    private val now = 1_700_000_000_000L

    @Test
    fun action_skipWhenRepeatPolicyBlocks() {
        assertEquals(AutoTextSendPlan.Action.SKIP, AutoTextSendPlan.action(maySend = false))
    }

    @Test
    fun action_recordBeforeSendWhenAllowed() {
        assertEquals(AutoTextSendPlan.Action.RECORD_THEN_SEND, AutoTextSendPlan.action(maySend = true))
    }

    @Test
    fun afterRecordingAttempt_retryIsBlockedWithin24Hours() {
        val recordedAttempt = TextedNumber(
            phoneNumber = "+16502530000",
            lastTextedAtEpochMs = now,
        )
        assertFalse(MissedCallChecker().shouldAutoText(recordedAttempt, now))
    }

    @Test
    fun recordedAttemptTimestampMatchesSendEpoch_preventsDuplicateOnWorkerRerun() {
        val sendEpochMs = now
        val recordedAttempt = TextedNumber(phoneNumber = "+16502530000", lastTextedAtEpochMs = sendEpochMs)
        assertEquals(
            AutoTextSendPlan.Action.SKIP,
            AutoTextSendPlan.action(MissedCallChecker().shouldAutoText(recordedAttempt, sendEpochMs)),
        )
        assertFalse(
            MissedCallChecker().shouldAutoText(recordedAttempt, sendEpochMs + TimeUnit.MINUTES.toMillis(5)),
        )
    }
}
