package com.callback.app.detect

import android.content.ContentResolver
import android.provider.CallLog
import com.callback.app.phone.PhoneNumberNormalizer

data class MissedCallLogEntry(
    val callLogId: Long,
    val phoneNumber: String,
    val dateEpochMs: Long,
)

/** Confirms missed calls via [CallLog.Calls] and supports dedupe by call log id. */
object MissedCallLogReader {
    private const val DEFAULT_LOOKBACK_MS = 5 * 60 * 1000L

    fun findRecentMissedCall(
        resolver: ContentResolver,
        candidateNumber: String,
        defaultRegion: String,
        nowEpochMs: Long = System.currentTimeMillis(),
        lookbackMs: Long = DEFAULT_LOOKBACK_MS,
    ): MissedCallLogEntry? {
        val sinceMs = nowEpochMs - lookbackMs
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE,
        )
        val selection = "${CallLog.Calls.TYPE} = ? AND ${CallLog.Calls.DATE} >= ?"
        val selectionArgs = arrayOf(
            CallLog.Calls.MISSED_TYPE.toString(),
            sinceMs.toString(),
        )
        val sortOrder = "${CallLog.Calls.DATE} DESC"

        resolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder,
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(CallLog.Calls._ID)
            val numberIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val dateIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
            while (cursor.moveToNext()) {
                val logNumber = cursor.getString(numberIdx) ?: continue
                if (PhoneNumberNormalizer.numbersMatch(candidateNumber, logNumber, defaultRegion)) {
                    return MissedCallLogEntry(
                        callLogId = cursor.getLong(idIdx),
                        phoneNumber = logNumber,
                        dateEpochMs = cursor.getLong(dateIdx),
                    )
                }
            }
        }
        return null
    }

    fun listUnprocessedMissedCalls(
        resolver: ContentResolver,
        processedCallLogIds: Set<Long>,
        defaultRegion: String,
        minCallLogIdExclusive: Long = 0L,
        nowEpochMs: Long = System.currentTimeMillis(),
        lookbackMs: Long = DEFAULT_LOOKBACK_MS,
    ): List<MissedCallLogEntry> {
        val sinceMs = nowEpochMs - lookbackMs
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE,
        )
        val selection = buildString {
            append("${CallLog.Calls.TYPE} = ? AND ${CallLog.Calls.DATE} >= ?")
            append(" AND ${CallLog.Calls._ID} > ?")
        }
        val selectionArgs = arrayOf(
            CallLog.Calls.MISSED_TYPE.toString(),
            sinceMs.toString(),
            minCallLogIdExclusive.toString(),
        )
        val sortOrder = "${CallLog.Calls.DATE} ASC"
        val results = mutableListOf<MissedCallLogEntry>()
        resolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder,
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(CallLog.Calls._ID)
            val numberIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val dateIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIdx)
                if (id in processedCallLogIds) continue
                val logNumber = cursor.getString(numberIdx) ?: continue
                results.add(
                    MissedCallLogEntry(
                        callLogId = id,
                        phoneNumber = logNumber,
                        dateEpochMs = cursor.getLong(dateIdx),
                    ),
                )
            }
        }
        return results
    }
}
