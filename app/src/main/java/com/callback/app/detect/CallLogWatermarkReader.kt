package com.callback.app.detect

import android.content.ContentResolver
import android.provider.CallLog

/** Reads call-log bounds used for onboarding watermarks. */
object CallLogWatermarkReader {
    fun readNewestCallLogId(resolver: ContentResolver): Long? {
        resolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls._ID),
            null,
            null,
            "${CallLog.Calls._ID} DESC LIMIT 1",
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getLong(0)
            }
        }
        return null
    }
}
