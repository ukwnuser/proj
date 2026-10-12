package com.callback.app.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InboundSmsHandlerTest {
    @Test
    fun storesPositiveReply() {
        assertEquals("call me", InboundSmsHandler.replyTextToStore("  call me  "))
    }

    @Test
    fun storesNonPositiveReply() {
        assertEquals("stop", InboundSmsHandler.replyTextToStore("stop"))
    }

    @Test
    fun ignoresBlank() {
        assertNull(InboundSmsHandler.replyTextToStore("   "))
        assertNull(InboundSmsHandler.replyTextToStore(null))
    }
}
