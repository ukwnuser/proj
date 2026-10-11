package com.callback.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leads")
data class Lead(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val displayName: String? = null,
    val isDone: Boolean = false,
    val createdAtEpochMs: Long,
)
