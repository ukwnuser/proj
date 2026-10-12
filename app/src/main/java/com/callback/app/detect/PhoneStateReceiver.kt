package com.callback.app.detect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.callback.app.data.AppDatabase
import com.callback.app.data.Lead
import com.callback.app.sms.AutoTextWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/** Receiver for incoming call state changes that detects missed calls and creates leads. */
class PhoneStateReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        Log.d(TAG, "PHONE_STATE received: state=$state, number=$incomingNumber")

        if (state == TelephonyManager.EXTRA_STATE_RINGING && incomingNumber != null) {
            return
        }

        if (state == TelephonyManager.EXTRA_STATE_IDLE && incomingNumber != null) {
            scope.launch {
                handleMissedCall(context, incomingNumber)
            }
        }
    }

    private suspend fun handleMissedCall(context: Context, phoneNumber: String) {
        val db = AppDatabase.getInstance(context)
        val existingLead = db.leadDao().findByPhoneNumber(phoneNumber)
        
        if (existingLead != null) {
            Log.d(TAG, "Lead already exists for $phoneNumber")
            return
        }

        val lead = Lead(
            phoneNumber = phoneNumber,
            displayName = resolveContactName(context, phoneNumber),
            isDone = false,
            createdAtEpochMs = System.currentTimeMillis(),
        )

        val leadId = db.leadDao().upsert(lead)
        Log.d(TAG, "Created lead $leadId for $phoneNumber")

        scheduleAutoText(context, phoneNumber)
    }

    private suspend fun scheduleAutoText(context: Context, phoneNumber: String) {
        val db = AppDatabase.getInstance(context)
        val texted = db.textedNumberDao().findByPhoneNumber(phoneNumber)
        val checker = MissedCallChecker()
        val now = System.currentTimeMillis()

        if (!checker.shouldAutoText(texted, now)) {
            Log.d(TAG, "Skipping auto-text for $phoneNumber - recently texted")
            return
        }

        val workData = Data.Builder()
            .putString(AutoTextWorker.KEY_PHONE_NUMBER, phoneNumber)
            .build()

        val request = OneTimeWorkRequestBuilder<AutoTextWorker>()
            .setInputData(workData)
            .setInitialDelay(30, TimeUnit.SECONDS)
            .addTag(AutoTextWorker.WORK_NAME_PREFIX + phoneNumber)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            AutoTextWorker.WORK_NAME_PREFIX + phoneNumber,
            ExistingWorkPolicy.KEEP,
            request,
        )
        Log.d(TAG, "Scheduled auto-text for $phoneNumber")
    }

    private fun resolveContactName(context: Context, phoneNumber: String): String? {
        return try {
            val uri = android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI
            val projection = arrayOf(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME)
            val cursor = context.contentResolver.query(
                android.net.Uri.withAppendedPath(uri, android.net.Uri.encode(phoneNumber)),
                projection,
                null,
                null,
                null,
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    it.getString(0)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve contact name for $phoneNumber", e)
            null
        }
    }

    companion object {
        private const val TAG = "PhoneStateReceiver"
    }
}
