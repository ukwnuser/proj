package com.callback.app.detect

import com.callback.app.data.Lead
import com.callback.app.data.ProcessedMissedCall
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MissedCallProcessorTest {
    private val now = 1_700_000_000_000L
    private val processor = MissedCallProcessor()

    @Test
    fun shouldProcessCallLogEntry_requiresMissedTypeRow() {
        assertEquals(
            MissedCallProcessor.SkipReason.NOT_CONFIRMED_IN_CALL_LOG,
            processor.shouldProcessCallLogEntry(alreadyProcessed = false, callLogEntry = null),
        )
    }

    @Test
    fun shouldProcessCallLogEntry_dedupesByCallLogId() {
        val entry = MissedCallLogEntry(callLogId = 42L, phoneNumber = "+15551234567", dateEpochMs = now)
        assertEquals(
            MissedCallProcessor.SkipReason.ALREADY_PROCESSED,
            processor.shouldProcessCallLogEntry(alreadyProcessed = true, callLogEntry = entry),
        )
        assertNull(processor.shouldProcessCallLogEntry(alreadyProcessed = false, callLogEntry = entry))
    }

    @Test
    fun resolveOpenLead_createsWhenNone() {
        assertTrue(processor.resolveOpenLead(null) is MissedCallProcessor.OpenLeadAction.CreateNew)
    }

    @Test
    fun resolveOpenLead_reusesExistingRegardlessOfAge() {
        val openLead = Lead(
            id = 7L,
            phoneNumber = "+16502530000",
            createdAtEpochMs = now - java.util.concurrent.TimeUnit.DAYS.toMillis(3),
        )
        val action = processor.resolveOpenLead(openLead)
        assertEquals(MissedCallProcessor.OpenLeadAction.ReuseExisting(7L), action)
    }

    @Test
    fun toProcessedRecord_usesCallLogIdForDedupe() {
        val entry = MissedCallLogEntry(callLogId = 99L, phoneNumber = "6502530000", dateEpochMs = now)
        val record = processor.toProcessedRecord(entry, "+16502530000")
        assertEquals(
            ProcessedMissedCall(callLogId = 99L, phoneNumber = "+16502530000", callDateEpochMs = now),
            record,
        )
    }
}
