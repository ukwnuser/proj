package com.callback.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TextedNumberDao {
    @Query("SELECT * FROM texted_numbers WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun findByPhoneNumber(phoneNumber: String): TextedNumber?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(textedNumber: TextedNumber)
}
