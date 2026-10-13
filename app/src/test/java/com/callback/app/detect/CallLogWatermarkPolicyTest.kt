package com.callback.app.detect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallLogWatermarkPolicyTest {
    @Test
    fun shouldInsert_whenNewestIdAheadOfCurrentMax() {
        assertTrue(CallLogWatermarkPolicy.shouldInsertWatermark(newestCallLogId = 120L, currentMaxProcessedId = 80L))
    }

    @Test
    fun shouldNotInsert_whenAlreadyAtOrPastNewest() {
        assertFalse(CallLogWatermarkPolicy.shouldInsertWatermark(newestCallLogId = 50L, currentMaxProcessedId = 50L))
        assertFalse(CallLogWatermarkPolicy.shouldInsertWatermark(newestCallLogId = 40L, currentMaxProcessedId = 50L))
    }

    @Test
    fun shouldNotInsert_whenCallLogEmpty() {
        assertFalse(CallLogWatermarkPolicy.shouldInsertWatermark(newestCallLogId = null, currentMaxProcessedId = null))
    }

    @Test
    fun buildWatermarkRecord_usesCallLogIdAsPrimaryKey() {
        val record = CallLogWatermarkPolicy.buildWatermarkRecord(newestCallLogId = 999L, nowEpochMs = 1_700_000_000_000L)
        assertEquals(999L, record.callLogId)
        assertEquals(CallLogWatermarkPolicy.WATERMARK_PHONE_NUMBER, record.phoneNumber)
    }
}
