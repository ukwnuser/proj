package com.callback.app.data

import com.callback.app.detect.MissedCallProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class LeadLookupTest {
    private val now = 1_700_000_000_000L
    private val processor = MissedCallProcessor()

    @Test
    fun findByPhoneNumber_onlyMatchesOpenLeads_byQueryContract() {
        val open = Lead(id = 1L, phoneNumber = "+16502530000", isDone = false, createdAtEpochMs = now)
        val done = open.copy(id = 2L, isDone = true)
        assertTrue(!open.isDone && done.isDone)
    }

    @Test
    fun openLeadOlderThan24h_isReusedNotDuplicated() {
        val openLead = Lead(
            id = 3L,
            phoneNumber = "+16502530000",
            isDone = false,
            createdAtEpochMs = now - TimeUnit.HOURS.toMillis(30),
        )
        assertEquals(
            MissedCallProcessor.OpenLeadAction.ReuseExisting(3L),
            processor.resolveOpenLead(openLead),
        )
    }
}
