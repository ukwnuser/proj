package com.callback.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processed_missed_calls")
data class ProcessedMissedCall(
    @PrimaryKey val callLogId: Long,
    val phoneNumber: String,
    val callDateEpochMs: Long,
)
