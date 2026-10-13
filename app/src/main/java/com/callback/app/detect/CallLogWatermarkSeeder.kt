package com.callback.app.detect

import android.content.Context
import com.callback.app.data.AppDatabase
import com.callback.app.data.ProcessedMissedCall

/** Seeds processed call-log watermark so pre-setup missed calls are ignored. */
object CallLogWatermarkPolicy {
    const val WATERMARK_PHONE_NUMBER: String = ""

    fun shouldInsertWatermark(newestCallLogId: Long?, currentMaxProcessedId: Long?): Boolean {
        if (newestCallLogId == null || newestCallLogId <= 0L) return false
        val current = currentMaxProcessedId ?: 0L
        return newestCallLogId > current
    }

    fun buildWatermarkRecord(newestCallLogId: Long, nowEpochMs: Long): ProcessedMissedCall =
        ProcessedMissedCall(
            callLogId = newestCallLogId,
            phoneNumber = WATERMARK_PHONE_NUMBER,
            callDateEpochMs = nowEpochMs,
        )
}

object CallLogWatermarkSeeder {
    suspend fun seedFromCurrentCallLog(context: Context, nowEpochMs: Long = System.currentTimeMillis()) {
        val newestId = CallLogWatermarkReader.readNewestCallLogId(context.contentResolver) ?: return
        val db = AppDatabase.getInstance(context)
        val currentMax = db.processedMissedCallDao().maxCallLogId()
        if (!CallLogWatermarkPolicy.shouldInsertWatermark(newestId, currentMax)) {
            return
        }
        db.processedMissedCallDao().insert(
            CallLogWatermarkPolicy.buildWatermarkRecord(newestId, nowEpochMs),
        )
    }
}
