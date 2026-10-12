package com.callback.app.detect

import com.callback.app.data.Lead
import com.callback.app.data.ProcessedMissedCall
import com.callback.app.detect.TextRepeatPolicy

/**
 * Pure orchestration for missed-call handling (testable without Android framework).
 */
class MissedCallProcessor(
    private val repeatPolicyMs: Long = TextRepeatPolicy.repeatWindowMs,
) {
    data class Decision(
        val shouldCreateLead: Boolean,
        val skipReason: SkipReason? = null,
    )

    enum class SkipReason {
        NOT_CONFIRMED_IN_CALL_LOG,
        ALREADY_PROCESSED,
        KNOWN_CONTACT,
        OPEN_LEAD_EXISTS,
        INVALID_NUMBER,
    }

    fun shouldProcessCallLogEntry(
        alreadyProcessed: Boolean,
        callLogEntry: MissedCallLogEntry?,
    ): SkipReason? {
        if (callLogEntry == null) return SkipReason.NOT_CONFIRMED_IN_CALL_LOG
        if (alreadyProcessed) return SkipReason.ALREADY_PROCESSED
        return null
    }

    fun shouldCreateLead(
        isKnownContact: Boolean,
        openLead: Lead?,
        nowEpochMs: Long,
    ): Decision {
        if (isKnownContact) {
            return Decision(shouldCreateLead = false, skipReason = SkipReason.KNOWN_CONTACT)
        }
        if (openLead != null && isWithinRepeatWindow(openLead.createdAtEpochMs, nowEpochMs)) {
            return Decision(shouldCreateLead = false, skipReason = SkipReason.OPEN_LEAD_EXISTS)
        }
        return Decision(shouldCreateLead = true)
    }

    fun isWithinRepeatWindow(leadCreatedAtEpochMs: Long, nowEpochMs: Long): Boolean =
        nowEpochMs - leadCreatedAtEpochMs < repeatPolicyMs

    fun toProcessedRecord(entry: MissedCallLogEntry, e164Phone: String): ProcessedMissedCall =
        ProcessedMissedCall(
            callLogId = entry.callLogId,
            phoneNumber = e164Phone,
            callDateEpochMs = entry.dateEpochMs,
        )
}
