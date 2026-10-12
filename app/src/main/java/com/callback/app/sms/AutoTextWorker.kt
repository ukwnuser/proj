package com.callback.app.sms

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.callback.app.data.AppDatabase
import com.callback.app.data.TextedNumber
import com.callback.app.detect.MissedCallChecker
import com.callback.app.phone.PhoneNumberNormalizer

/**
 * Sends an auto-text for a missed call.
 * Persists [TextedNumber] when send is attempted.
 */
class AutoTextWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val rawNumber = inputData.getString(KEY_PHONE_NUMBER) ?: return Result.failure()
        val region = PhoneNumberNormalizer.defaultRegion(applicationContext)
        val phoneNumber = PhoneNumberNormalizer.toE164(rawNumber, region) ?: rawNumber
        val message = inputData.getString(KEY_MESSAGE) ?: DEFAULT_MESSAGE
        val now = System.currentTimeMillis()

        val db = AppDatabase.getInstance(applicationContext)
        val texted = db.textedNumberDao().findByPhoneNumber(phoneNumber)
        if (!MissedCallChecker().shouldAutoText(texted, now)) {
            return Result.success()
        }

        applicationContext.requireSmsManager()
            .sendTextMessage(phoneNumber, null, message, null, null)

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
