package com.callback.app.detect

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.callback.app.phone.PhoneNumberNormalizer

/** Returns true when [phoneNumber] matches an entry in the device contacts. */
object ContactLookup {
    fun isKnownContact(context: Context, phoneNumber: String): Boolean {
        val region = PhoneNumberNormalizer.defaultRegion(context)
        val e164 = PhoneNumberNormalizer.toE164(phoneNumber, region) ?: phoneNumber
        return lookupDisplayName(context, e164) != null ||
            lookupDisplayName(context, phoneNumber) != null
    }

    fun lookupDisplayName(context: Context, phoneNumber: String): String? {
        return try {
            val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            val cursor = context.contentResolver.query(
                Uri.withAppendedPath(uri, Uri.encode(phoneNumber)),
                projection,
                null,
                null,
                null,
            )
            cursor?.use {
                if (it.moveToFirst()) it.getString(0) else null
            }
        } catch (_: Exception) {
            null
        }
    }
}
