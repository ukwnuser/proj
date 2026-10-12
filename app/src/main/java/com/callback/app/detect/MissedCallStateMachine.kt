package com.callback.app.detect

/**
 * Tracks RINGING → OFFHOOK → IDLE transitions.
 * A missed call is RINGING followed by IDLE with no OFFHOOK in between.
 */
class MissedCallStateMachine {
    companion object {
        const val STATE_RINGING = "RINGING"
        const val STATE_OFFHOOK = "OFFHOOK"
        const val STATE_IDLE = "IDLE"
    }
    private var ringing = false
    private var offhook = false
    private var pendingNumber: String? = null

    fun onPhoneState(state: String?, incomingNumber: String?): String? {
        return when (state) {
            STATE_RINGING -> {
                offhook = false
                ringing = true
                if (!incomingNumber.isNullOrBlank()) {
                    pendingNumber = incomingNumber
                }
                null
            }
            STATE_OFFHOOK -> {
                offhook = true
                null
            }
            STATE_IDLE -> {
                val number = pendingNumber?.takeIf { it.isNotBlank() }
                    ?: incomingNumber?.takeIf { it.isNotBlank() }
                val missed = ringing && !offhook && number != null
                resetCallLeg()
                if (missed) number else null
            }
            else -> null
        }
    }

    fun resetCallLeg() {
        ringing = false
        offhook = false
        pendingNumber = null
    }

    fun reset() {
        resetCallLeg()
    }
}
