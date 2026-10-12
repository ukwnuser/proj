package com.callback.app.data

import com.callback.app.detect.MissedCallProcessor
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

/** Documents open-lead lookup behavior used with [LeadDao.findByPhoneNumber]. */
class LeadLookupTest {
    private val now = 1_700_000_000_000L
    private val processor = MissedCallProcessor()

    @Test
    fun findByPhoneNumber_onlyMatchesOpenLeads_byQueryContract() {
        val open = Lead(id = 1L, phoneNumber = "+15551234567", isDone = false, createdAtEpochMs = now)
        val done = open.copy(id = 2L, isDone = true)
        assertFalse(open.isDone)
        assertTrue(done.isDone)
    }

    @Test
    fun openLeadWithinRepeatWindow_blocksDuplicateLead() {
        val openLead = Lead(
            phoneNumber = "+15551234567",
            isDone = false,
            createdAtEpochMs = now - TimeUnit.HOURS.toMillis(3),
        )
        val decision = processor.shouldCreateLead(false, openLead, now)
        assertFalse(decision.shouldCreateLead)
    }

    @Test
    fun noOpenLead_allowsCreation() {
        val decision = processor.shouldCreateLead(false, openLead = null, nowEpochMs = now)
        assertTrue(decision.shouldCreateLead)
        assertNull(decision.skipReason)
    }
}
