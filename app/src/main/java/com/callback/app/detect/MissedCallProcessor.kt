package com.callback.app.detect

import com.callback.app.data.Lead
import com.callback.app.data.ProcessedMissedCall

/** Pure orchestration for missed-call handling (testable without Android framework). */
class MissedCallProcessor {
    sealed class OpenLeadAction {
        data object CreateNew : OpenLeadAction()
        data class ReuseExisting(val leadId: Long) : OpenLeadAction()
    }

    enum class SkipReason {
        NOT_CONFIRMED_IN_CALL_LOG,
        ALREADY_PROCESSED,
        KNOWN_CONTACT,
        CONTACT_CHECK_INDETERMINATE,
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

    fun resolveOpenLead(openLead: Lead?): OpenLeadAction =
        if (openLead == null) OpenLeadAction.CreateNew else OpenLeadAction.ReuseExisting(openLead.id)

    fun toProcessedRecord(entry: MissedCallLogEntry, e164Phone: String): ProcessedMissedCall =
        ProcessedMissedCall(
            callLogId = entry.callLogId,
            phoneNumber = e164Phone,
            callDateEpochMs = entry.dateEpochMs,
        )
}
