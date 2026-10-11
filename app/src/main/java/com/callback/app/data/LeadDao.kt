package com.callback.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {
    @Query("SELECT * FROM leads WHERE isDone = 0 ORDER BY createdAtEpochMs DESC")
    fun observeNewLeads(): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE isDone = 1 ORDER BY createdAtEpochMs DESC")
    fun observeDoneLeads(): Flow<List<Lead>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(lead: Lead): Long
}
