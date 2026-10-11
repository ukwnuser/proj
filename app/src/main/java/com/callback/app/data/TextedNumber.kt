package com.callback.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "texted_numbers")
data class TextedNumber(
    @PrimaryKey val phoneNumber: String,
    /** Last auto-text timestamp; used for 24h repeat guard. */
    val lastTextedAtEpochMs: Long,
)
