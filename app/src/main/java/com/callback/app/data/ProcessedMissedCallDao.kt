package com.callback.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProcessedMissedCallDao {
    @Query("SELECT * FROM processed_missed_calls WHERE callLogId = :callLogId LIMIT 1")
    suspend fun findByCallLogId(callLogId: Long): ProcessedMissedCall?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(processed: ProcessedMissedCall): Long
}
