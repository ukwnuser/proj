package com.callback.app.detect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

/** Stub receiver for incoming call state changes. */
class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        Log.d(TAG, "PHONE_STATE received (stub)")
    }

    companion object {
        private const val TAG = "PhoneStateReceiver"
    }
}
