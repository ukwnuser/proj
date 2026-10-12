package com.callback.app.detect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.callback.app.phone.PhoneNumberNormalizer

object ContactLookup {
    fun hasReadContactsPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    fun classify(context: Context, phoneNumber: String): ContactLookupResult {
        if (!hasReadContactsPermission(context)) {
            return ContactLookupResult.Indeterminate
        }
        val region = PhoneNumberNormalizer.defaultRegion(context)
        val e164 = PhoneNumberNormalizer.toE164(phoneNumber, region) ?: phoneNumber
        val lookup = lookupDisplayNameInternal(context, listOf(e164, phoneNumber))
        return ContactClassifier.classify(
            hasReadContactsPermission = true,
            lookupFailed = lookup.failed,
            displayName = lookup.displayName,
        )
    }

    fun lookupDisplayName(context: Context, phoneNumber: String): String? {
        if (!hasReadContactsPermission(context)) return null
        return lookupDisplayNameInternal(context, listOf(phoneNumber)).displayName
    }

    private data class LookupOutcome(val displayName: String?, val failed: Boolean)

    private fun lookupDisplayNameInternal(context: Context, numbers: List<String>): LookupOutcome {
        var failed = false
        for (number in numbers.distinct()) {
            try {
                val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI
                val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
                val cursor = context.contentResolver.query(
                    Uri.withAppendedPath(uri, Uri.encode(number)),
                    projection,
                    null,
                    null,
                    null,
                )
                val name = cursor?.use {
                    if (it.moveToFirst()) it.getString(0) else null
                }
                if (name != null) return LookupOutcome(name, failed = false)
            } catch (_: Exception) {
                failed = true
            }
        }
        return LookupOutcome(displayName = null, failed = failed)
    }
}
