package com.callback.app.sms

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.callback.app.data.AppDatabase
import com.callback.app.phone.PhoneNumberNormalizer
import com.callback.app.util.SafeLog

/** Handles inbound SMS and stores positive replies on open leads. */
class IncomingSmsWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val rawNumber = inputData.getString(KEY_PHONE_NUMBER) ?: return Result.failure()
        val body = inputData.getString(KEY_MESSAGE_BODY).orEmpty()
        val region = PhoneNumberNormalizer.defaultRegion(applicationContext)
        val e164 = PhoneNumberNormalizer.toE164(rawNumber, region) ?: rawNumber

        val db = AppDatabase.getInstance(applicationContext)
        val lead = db.leadDao().findByPhoneNumber(e164)
            ?: db.leadDao().findByPhoneNumber(rawNumber)

        if (lead == null) {
            SafeLog.phoneEvent(TAG, "Inbound SMS: no open lead")
            return Result.success()
        }

        if (ReplyParser.isPositiveReply(body)) {
            db.leadDao().updateReplyText(lead.id, body.trim())
            SafeLog.phoneEvent(TAG, "Stored positive reply on open lead")
        } else {
            SafeLog.phoneEvent(TAG, "Inbound SMS: non-positive reply")
        }
        return Result.success()
    }

    companion object {
        const val KEY_PHONE_NUMBER = "phone_number"
        const val KEY_MESSAGE_BODY = "message_body"
        private const val TAG = "IncomingSmsWorker"
    }
}
