package com.callback.app.phone

import android.content.Context
import android.os.Build
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

/** Normalizes phone numbers to E.164 for storage and comparison. */
object PhoneNumberNormalizer {
    private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    fun defaultRegion(context: Context): String {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val simCountry = telephony?.simCountryIso?.takeIf { it.isNotBlank() }?.uppercase(Locale.US)
        if (simCountry != null) return simCountry
        return Locale.getDefault().country.ifBlank { "US" }
    }

    fun toE164(raw: String, defaultRegion: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null

        val fromLib = try {
            val parsed = phoneUtil.parse(trimmed, defaultRegion)
            if (phoneUtil.isValidNumber(parsed)) {
                phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
            } else {
                null
            }
        } catch (_: NumberParseException) {
            null
        }
        if (!fromLib.isNullOrBlank()) return fromLib

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PhoneNumberUtils.formatNumberToE164(trimmed, defaultRegion)
        } else {
            null
        }
    }

    fun numbersMatch(a: String, b: String, defaultRegion: String): Boolean {
        val e164A = toE164(a, defaultRegion) ?: a.trim()
        val e164B = toE164(b, defaultRegion) ?: b.trim()
        return e164A == e164B
    }
}
