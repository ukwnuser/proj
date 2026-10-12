package com.callback.app.detect

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.callback.app.data.AppDatabase
import com.callback.app.data.Lead
import com.callback.app.phone.PhoneNumberNormalizer
import com.callback.app.sms.AutoTextWorker
import com.callback.app.util.SafeLog
import java.util.concurrent.TimeUnit

/** Confirms, dedupes, and creates or reuses leads for missed calls from the call log. */
class ProcessMissedCallWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val rawHint = inputData.getString(KEY_RAW_PHONE_NUMBER)
        val region = PhoneNumberNormalizer.defaultRegion(applicationContext)
        val db = AppDatabase.getInstance(applicationContext)
        val processedIds = db.processedMissedCallDao().getAllCallLogIds().toSet()
        val minLogId = db.processedMissedCallDao().maxCallLogId() ?: 0L

        val entries = resolveMissedEntries(rawHint, region, processedIds, minLogId)
        if (entries.isEmpty() && !rawHint.isNullOrBlank()) {
            val foundForHint = MissedCallLogReader.findRecentMissedCall(
                applicationContext.contentResolver,
                rawHint,
                region,
            ) != null
            if (CallLogRetryPolicy.shouldRetry(runAttemptCount, foundForHint)) {
                return Result.retry()
            }
        }

        if (entries.isEmpty()) {
            return Result.success()
        }

        for (entry in entries) {
            processMissedEntry(entry, region, db)
        }
        return Result.success()
    }

    private fun resolveMissedEntries(
        rawHint: String?,
        region: String,
        processedIds: Set<Long>,
        minLogId: Long,
    ): List<MissedCallLogEntry> {
        val resolver = applicationContext.contentResolver
        if (!rawHint.isNullOrBlank()) {
            val matched = MissedCallLogReader.findRecentMissedCall(resolver, rawHint, region)
            if (matched != null && matched.callLogId !in processedIds) {
                return listOf(matched)
            }
        }
        return MissedCallLogReader.listUnprocessedMissedCalls(
            resolver = resolver,
            processedCallLogIds = processedIds,
            defaultRegion = region,
            minCallLogIdExclusive = minLogId,
        )
    }

    private suspend fun processMissedEntry(
        entry: MissedCallLogEntry,
        region: String,
        db: AppDatabase,
    ) {
        val processor = MissedCallProcessor()
        if (db.processedMissedCallDao().findByCallLogId(entry.callLogId) != null) {
            return
        }

        val e164 = PhoneNumberNormalizer.toE164(entry.phoneNumber, region)
        if (e164 == null) {
            SafeLog.phoneEvent(TAG, "Skipped missed call: invalid number")
            return
        }

        when (val contact = ContactLookup.classify(applicationContext, entry.phoneNumber)) {
            ContactLookupResult.IsContact -> {
                SafeLog.phoneEvent(TAG, "Skipped missed call: known contact")
                return
            }
            ContactLookupResult.Indeterminate -> {
                SafeLog.phoneEvent(TAG, "Skipped missed call: contact check indeterminate")
                return
            }
            ContactLookupResult.NotContact -> Unit
        }

        processor.shouldProcessCallLogEntry(alreadyProcessed = false, callLogEntry = entry)?.let {
            SafeLog.phoneEvent(TAG, "Skipped missed call: $it")
            return
        }

        val inserted = db.processedMissedCallDao().insert(processor.toProcessedRecord(entry, e164))
        if (inserted == -1L) {
            SafeLog.phoneEvent(TAG, "Skipped missed call: dedupe insert conflict")
            return
        }

        val now = System.currentTimeMillis()
        val openLead = db.leadDao().findByPhoneNumber(e164)
        when (val action = processor.resolveOpenLead(openLead)) {
            is MissedCallProcessor.OpenLeadAction.CreateNew -> {
                val lead = Lead(
                    phoneNumber = e164,
                    displayName = ContactLookup.lookupDisplayName(applicationContext, e164),
                    isDone = false,
                    createdAtEpochMs = now,
                )
                db.leadDao().upsert(lead)
                SafeLog.phoneEvent(TAG, "Created lead for missed call")
            }
            is MissedCallProcessor.OpenLeadAction.ReuseExisting -> {
                db.leadDao().touchLead(action.leadId, now)
                SafeLog.phoneEvent(TAG, "Reused open lead for missed call")
            }
        }
        scheduleAutoTextIfAllowed(e164)
    }

    private suspend fun scheduleAutoTextIfAllowed(e164: String) {
        if (!ContactClassifier.maySendAutoText(ContactLookup.classify(applicationContext, e164))) {
            SafeLog.phoneEvent(TAG, "Skipping auto-text: contact policy")
            return
        }

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

        fun buildRequest(rawPhoneHint: String?): OneTimeWorkRequest {
            val data = Data.Builder()
            if (!rawPhoneHint.isNullOrBlank()) {
                data.putString(KEY_RAW_PHONE_NUMBER, rawPhoneHint)
            }
            return OneTimeWorkRequestBuilder<ProcessMissedCallWorker>()
                .setInputData(data.build())
                .setInitialDelay(CallLogRetryPolicy.INITIAL_DELAY_SECONDS, TimeUnit.SECONDS)
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    CallLogRetryPolicy.BACKOFF_DELAY_SECONDS,
                    TimeUnit.SECONDS,
                )
                .build()
        }
    }
}
