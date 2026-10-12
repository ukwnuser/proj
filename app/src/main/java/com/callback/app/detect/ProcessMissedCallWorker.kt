package com.callback.app.detect

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.callback.app.data.AppDatabase
import com.callback.app.data.Lead
import com.callback.app.phone.PhoneNumberNormalizer
import com.callback.app.sms.AutoTextWorker
import com.callback.app.util.SafeLog
import java.util.concurrent.TimeUnit

/** Confirms, dedupes, and creates leads for missed calls detected by [PhoneStateReceiver]. */
class ProcessMissedCallWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val rawNumber = inputData.getString(KEY_RAW_PHONE_NUMBER) ?: return Result.failure()
        val region = PhoneNumberNormalizer.defaultRegion(applicationContext)
        val e164 = PhoneNumberNormalizer.toE164(rawNumber, region)
        if (e164 == null) {
            SafeLog.phoneEvent(TAG, "Skipped missed call: invalid number")
            return Result.success()
        }

        if (ContactLookup.isKnownContact(applicationContext, rawNumber)) {
            SafeLog.phoneEvent(TAG, "Skipped missed call: known contact")
            return Result.success()
        }

        val logEntry = MissedCallLogReader.findRecentMissedCall(
            applicationContext.contentResolver,
            rawNumber,
            region,
        )
        val db = AppDatabase.getInstance(applicationContext)
        val processor = MissedCallProcessor()
        val alreadyProcessed = logEntry?.let {
            db.processedMissedCallDao().findByCallLogId(it.callLogId) != null
        } ?: false

        processor.shouldProcessCallLogEntry(alreadyProcessed, logEntry)?.let { reason ->
            SafeLog.phoneEvent(TAG, "Skipped missed call: $reason")
            return Result.success()
        }
        val entry = logEntry!!

        val inserted = db.processedMissedCallDao().insert(
            processor.toProcessedRecord(entry, e164),
        )
        if (inserted == -1L) {
            SafeLog.phoneEvent(TAG, "Skipped missed call: dedupe insert conflict")
            return Result.success()
        }

        val now = System.currentTimeMillis()
        val openLead = db.leadDao().findByPhoneNumber(e164)
        val decision = processor.shouldCreateLead(
            isKnownContact = false,
            openLead = openLead,
            nowEpochMs = now,
        )
        if (!decision.shouldCreateLead) {
            SafeLog.phoneEvent(TAG, "Skipped lead create: ${decision.skipReason}")
            scheduleAutoTextIfAllowed(e164)
            return Result.success()
        }

        val lead = Lead(
            phoneNumber = e164,
            displayName = ContactLookup.lookupDisplayName(applicationContext, e164),
            isDone = false,
            createdAtEpochMs = now,
        )
        db.leadDao().upsert(lead)
        SafeLog.phoneEvent(TAG, "Created lead for missed call")
        scheduleAutoTextIfAllowed(e164)
        return Result.success()
    }

    private suspend fun scheduleAutoTextIfAllowed(e164: String) {
        val db = AppDatabase.getInstance(applicationContext)
        val texted = db.textedNumberDao().findByPhoneNumber(e164)
        val checker = MissedCallChecker()
        val now = System.currentTimeMillis()
        if (!checker.shouldAutoText(texted, now)) {
            SafeLog.phoneEvent(TAG, "Skipping auto-text: repeat policy")
            return
        }

        val workData = Data.Builder()
            .putString(AutoTextWorker.KEY_PHONE_NUMBER, e164)
            .build()

        val request = OneTimeWorkRequestBuilder<AutoTextWorker>()
            .setInputData(workData)
            .setInitialDelay(30, TimeUnit.SECONDS)
            .addTag(AutoTextWorker.WORK_NAME_PREFIX + e164)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            AutoTextWorker.WORK_NAME_PREFIX + e164,
            ExistingWorkPolicy.KEEP,
            request,
        )
        SafeLog.phoneEvent(TAG, "Scheduled auto-text work")
    }

    companion object {
        const val KEY_RAW_PHONE_NUMBER = "raw_phone_number"
        private const val TAG = "ProcessMissedCallWorker"
    }
}
