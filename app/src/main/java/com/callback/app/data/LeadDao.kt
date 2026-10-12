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

    @Query("UPDATE leads SET isDone = 1 WHERE id = :leadId")
    suspend fun markAsDone(leadId: Long)

    @Query("SELECT * FROM leads WHERE phoneNumber = :phoneNumber AND isDone = 0 LIMIT 1")
    suspend fun findByPhoneNumber(phoneNumber: String): Lead?

    @Query("UPDATE leads SET replyText = :replyText WHERE id = :leadId")
    suspend fun updateReplyText(leadId: Long, replyText: String)

    @Query("UPDATE leads SET createdAtEpochMs = :atMs WHERE id = :leadId")
    suspend fun touchLead(leadId: Long, atMs: Long)

    @Query("DELETE FROM leads WHERE id = :leadId")
    suspend fun delete(leadId: Long)
}
