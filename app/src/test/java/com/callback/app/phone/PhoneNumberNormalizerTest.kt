package com.callback.app.phone

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberNormalizerTest {
    private val sampleNational = "6502530000"
    private val sampleE164 = "+16502530000"

    @Test
    fun toE164_usNationalNumber() {
        val e164 = PhoneNumberNormalizer.toE164("(650) 253-0000", "US")
        assertEquals(sampleE164, e164)
    }

    @Test
    fun toE164_alreadyE164() {
        val e164 = PhoneNumberNormalizer.toE164(sampleE164, "US")
        assertEquals(sampleE164, e164)
    }

    @Test
    fun numbersMatch_differentFormats() {
        assertTrue(
            PhoneNumberNormalizer.numbersMatch(sampleNational, "+1 650-253-0000", "US"),
        )
    }

    @Test
    fun toE164_invalidReturnsNull() {
        org.junit.Assert.assertNull(PhoneNumberNormalizer.toE164("not-a-phone", "US"))
    }
}
