package com.callback.app.sms

import android.content.Context
import android.os.Build
import android.telephony.SmsManager

fun Context.requireSmsManager(): SmsManager {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService(SmsManager::class.java)
    } else {
        @Suppress("DEPRECATION")
        SmsManager.getDefault()
    }
}
