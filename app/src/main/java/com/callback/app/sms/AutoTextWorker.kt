package com.callback.app.sms

import android.content.Context
import android.telephony.SmsManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.callback.app.data.AppDatabase
import com.callback.app.data.TextedNumber
import com.callback.app.detect.MissedCallChecker

/**
 * Sends an auto-text for a missed call (stub message body).
 * Persists [TextedNumber] when send is attempted.
 */
class AutoTextWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val phoneNumber = inputData.getString(KEY_PHONE_NUMBER) ?: return Result.failure()
        val message = inputData.getString(KEY_MESSAGE) ?: DEFAULT_MESSAGE
        val now = System.currentTimeMillis()

        val db = AppDatabase.getInstance(applicationContext)
        val texted = db.textedNumberDao().findByPhoneNumber(phoneNumber)
        if (!MissedCallChecker().shouldAutoText(texted, now)) {
            return Result.success()
        }

        @Suppress("DEPRECATION")
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)

        db.textedNumberDao().upsert(
            TextedNumber(phoneNumber = phoneNumber, lastTextedAtEpochMs = now),
        )
        return Result.success()
    }

    companion object {
        const val KEY_PHONE_NUMBER = "phone_number"
        const val KEY_MESSAGE = "message"
        const val DEFAULT_MESSAGE = "Sorry we missed your call — reply CALL ME when you're free."
        const val WORK_NAME_PREFIX = "auto_text_"
    }
}
