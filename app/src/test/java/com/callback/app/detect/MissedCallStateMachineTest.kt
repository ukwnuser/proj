package com.callback.app.detect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class MissedCallStateMachineTest {
    private lateinit var machine: MissedCallStateMachine

    companion object {
        private const val RINGING = "RINGING"
        private const val OFFHOOK = "OFFHOOK"
        private const val IDLE = "IDLE"
    }

    @Before
    fun setUp() {
        machine = MissedCallStateMachine()
    }

    @Test
    fun ringingThenIdle_withoutOffhook_isMissed() {
        machine.onPhoneState(RINGING, "+15551234567")
        val missed = machine.onPhoneState(IDLE, null)
        assertEquals("+15551234567", missed)
    }

    @Test
    fun ringingThenOffhookThenIdle_isNotMissed() {
        machine.onPhoneState(RINGING, "+15551234567")
        machine.onPhoneState(OFFHOOK, null)
        val missed = machine.onPhoneState(IDLE, null)
        assertNull(missed)
    }

    @Test
    fun idleWithoutRinging_isNotMissed() {
        val missed = machine.onPhoneState(IDLE, "+15551234567")
        assertNull(missed)
    }

    @Test
    fun ringingUsesNumberFromIdleWhenPending() {
        machine.onPhoneState(RINGING, "+15559876543")
        val missed = machine.onPhoneState(IDLE, "+15550001111")
        assertEquals("+15559876543", missed)
    }
}
