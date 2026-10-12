package com.callback.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.callback.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Receiver for inbound SMS that updates lead status on positive reply. */
class SmsReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val originatingAddress = messages[0].originatingAddress
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }

        Log.d(TAG, "SMS received from $originatingAddress: $body")

        if (originatingAddress != null) {
            scope.launch {
                handleIncomingSms(context, originatingAddress, body)
            }
        }
    }

    private suspend fun handleIncomingSms(context: Context, phoneNumber: String, body: String) {
        val db = AppDatabase.getInstance(context)
        val lead = db.leadDao().findByPhoneNumber(phoneNumber)

        if (lead == null) {
            Log.d(TAG, "No lead found for $phoneNumber")
            return
        }

        if (ReplyParser.isPositiveReply(body)) {
            db.leadDao().markAsDone(lead.id)
            Log.d(TAG, "Marked lead ${lead.id} as done for positive reply from $phoneNumber")
        } else {
            Log.d(TAG, "Non-positive reply from $phoneNumber: $body")
        }
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
