package com.callback.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

/** Stub receiver for inbound SMS (reply handling wired in a later phase). */
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
        val positive = ReplyParser.isPositiveReply(body)
        Log.d(TAG, "SMS received (stub), positive=$positive")
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
