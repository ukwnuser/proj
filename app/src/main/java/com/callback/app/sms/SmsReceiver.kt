package com.callback.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.callback.app.util.SafeLog

/** Receiver for inbound SMS that enqueues processing for positive replies. */
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val originatingAddress = messages[0].originatingAddress ?: return
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }

        SafeLog.phoneEvent(TAG, "SMS received")

        val workData = Data.Builder()
            .putString(IncomingSmsWorker.KEY_PHONE_NUMBER, originatingAddress)
            .putString(IncomingSmsWorker.KEY_MESSAGE_BODY, body)
            .build()

        val request = OneTimeWorkRequestBuilder<IncomingSmsWorker>()
            .setInputData(workData)
            .addTag(WORK_TAG_PREFIX + originatingAddress.hashCode())
            .build()

        WorkManager.getInstance(context.applicationContext).enqueue(request)
    }

    companion object {
        private const val TAG = "SmsReceiver"
        private const val WORK_TAG_PREFIX = "incoming_sms_"
    }
}
