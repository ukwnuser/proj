package com.callback.app.detect

/** Result of checking whether a number belongs to a contact. */
sealed class ContactLookupResult {
    data object IsContact : ContactLookupResult()
    data object NotContact : ContactLookupResult()
    /** Permission missing or lookup failed — treat as do-not-text (fail closed). */
    data object Indeterminate : ContactLookupResult()
}

object ContactClassifier {
    fun classify(
        hasReadContactsPermission: Boolean,
        lookupFailed: Boolean,
        displayName: String?,
    ): ContactLookupResult {
        if (!hasReadContactsPermission || lookupFailed) return ContactLookupResult.Indeterminate
        return if (displayName != null) ContactLookupResult.IsContact else ContactLookupResult.NotContact
    }

    fun maySendAutoText(result: ContactLookupResult): Boolean =
        result is ContactLookupResult.NotContact
}
