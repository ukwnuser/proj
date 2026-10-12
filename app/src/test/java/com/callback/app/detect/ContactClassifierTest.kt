package com.callback.app.detect

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactClassifierTest {
    @Test
    fun failClosed_whenPermissionMissing() {
        val result = ContactClassifier.classify(
            hasReadContactsPermission = false,
            lookupFailed = false,
            displayName = null,
        )
        assertTrue(result is ContactLookupResult.Indeterminate)
        assertFalse(ContactClassifier.maySendAutoText(result))
    }

    @Test
    fun failClosed_whenLookupThrows() {
        val result = ContactClassifier.classify(
            hasReadContactsPermission = true,
            lookupFailed = true,
            displayName = null,
        )
        assertTrue(result is ContactLookupResult.Indeterminate)
        assertFalse(ContactClassifier.maySendAutoText(result))
    }

    @Test
    fun notContact_allowsAutoText() {
        val result = ContactClassifier.classify(
            hasReadContactsPermission = true,
            lookupFailed = false,
            displayName = null,
        )
        assertTrue(result is ContactLookupResult.NotContact)
        assertTrue(ContactClassifier.maySendAutoText(result))
    }

    @Test
    fun isContact_blocksAutoText() {
        val result = ContactClassifier.classify(
            hasReadContactsPermission = true,
            lookupFailed = false,
            displayName = "Alice",
        )
        assertTrue(result is ContactLookupResult.IsContact)
        assertFalse(ContactClassifier.maySendAutoText(result))
    }
}
