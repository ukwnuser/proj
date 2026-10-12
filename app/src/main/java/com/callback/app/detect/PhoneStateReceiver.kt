package com.callback.app.detect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import androidx.work.WorkManager
import com.callback.app.util.SafeLog

/**
 * Enqueues missed-call processing on IDLE. Call-log scan covers process death
 * (in-memory ringing state is only a hint).
 */
class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        SafeLog.phoneEvent(TAG, "PHONE_STATE received: state=$state")

        val missedHint = stateMachine.onPhoneState(state, incomingNumber)
        if (state != MissedCallStateMachine.STATE_IDLE) return

        WorkManager.getInstance(context.applicationContext)
            .enqueue(ProcessMissedCallWorker.buildRequest(missedHint))
        SafeLog.phoneEvent(TAG, "Enqueued missed-call processing work")
    }

    companion object {
        private const val TAG = "PhoneStateReceiver"
        private val stateMachine = MissedCallStateMachine()
    }
}
