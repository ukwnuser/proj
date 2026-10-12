package com.callback.app.detect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.callback.app.util.SafeLog

/** Receiver for incoming call state changes that detects missed calls and enqueues processing. */
class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        SafeLog.phoneEvent(TAG, "PHONE_STATE received: state=$state")

        val missedNumber = stateMachine.onPhoneState(state, incomingNumber) ?: return

        val workData = Data.Builder()
            .putString(ProcessMissedCallWorker.KEY_RAW_PHONE_NUMBER, missedNumber)
            .build()

        val request = OneTimeWorkRequestBuilder<ProcessMissedCallWorker>()
            .setInputData(workData)
            .addTag(WORK_TAG_PREFIX + missedNumber.hashCode())
            .build()

        WorkManager.getInstance(context.applicationContext).enqueue(request)
        SafeLog.phoneEvent(TAG, "Enqueued missed-call processing work")
    }

    companion object {
        private const val TAG = "PhoneStateReceiver"
        private const val WORK_TAG_PREFIX = "process_missed_"
        private val stateMachine = MissedCallStateMachine()
    }
}
