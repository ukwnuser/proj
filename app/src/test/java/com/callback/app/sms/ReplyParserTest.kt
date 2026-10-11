package com.callback.app.sms

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReplyParserTest {
    @Test
    fun positiveReply_callMe() {
        assertTrue(ReplyParser.isPositiveReply("Please call me when you can"))
    }

    @Test
    fun positiveReply_yes() {
        assertTrue(ReplyParser.isPositiveReply("YES"))
    }

    @Test
    fun negativeReply_unrelated() {
        assertFalse(ReplyParser.isPositiveReply("stop texting me"))
    }

    @Test
    fun emptyReply_isNotPositive() {
        assertFalse(ReplyParser.isPositiveReply(""))
        assertFalse(ReplyParser.isPositiveReply(null))
    }
}
