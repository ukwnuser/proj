package com.callback.app.detect

import com.callback.app.data.Lead
import com.callback.app.data.ProcessedMissedCall
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

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
    fun shouldCreateLead_skipsKnownContacts() {
        val decision = processor.shouldCreateLead(isKnownContact = true, openLead = null, nowEpochMs = now)
        assertFalse(decision.shouldCreateLead)
        assertEquals(MissedCallProcessor.SkipReason.KNOWN_CONTACT, decision.skipReason)
    }

    @Test
    fun shouldCreateLead_skipsWhenOpenLeadWithin24Hours() {
        val openLead = Lead(
            id = 1L,
            phoneNumber = "+15551234567",
            createdAtEpochMs = now - TimeUnit.HOURS.toMillis(2),
        )
        val decision = processor.shouldCreateLead(isKnownContact = false, openLead = openLead, nowEpochMs = now)
        assertFalse(decision.shouldCreateLead)
        assertEquals(MissedCallProcessor.SkipReason.OPEN_LEAD_EXISTS, decision.skipReason)
    }

    @Test
    fun shouldCreateLead_allowsWhenOpenLeadOlderThan24Hours() {
        val openLead = Lead(
            id = 1L,
            phoneNumber = "+15551234567",
            createdAtEpochMs = now - TimeUnit.HOURS.toMillis(25),
        )
        val decision = processor.shouldCreateLead(isKnownContact = false, openLead = openLead, nowEpochMs = now)
        assertTrue(decision.shouldCreateLead)
    }

    @Test
    fun toProcessedRecord_usesCallLogIdForDedupe() {
        val entry = MissedCallLogEntry(callLogId = 99L, phoneNumber = "5551234567", dateEpochMs = now)
        val record = processor.toProcessedRecord(entry, "+15551234567")
        assertEquals(
            ProcessedMissedCall(callLogId = 99L, phoneNumber = "+15551234567", callDateEpochMs = now),
            record,
        )
    }

    @Test
    fun isWithinRepeatWindow_matchesTextRepeatPolicy() {
        assertTrue(processor.isWithinRepeatWindow(now - TimeUnit.HOURS.toMillis(1), now))
        assertFalse(processor.isWithinRepeatWindow(now - TimeUnit.HOURS.toMillis(25), now))
    }
}
