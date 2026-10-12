package com.callback.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Lead::class, TextedNumber::class, ProcessedMissedCall::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun leadDao(): LeadDao
    abstract fun textedNumberDao(): TextedNumberDao
    abstract fun processedMissedCallDao(): ProcessedMissedCallDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE leads ADD COLUMN replyText TEXT")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS processed_missed_calls (
                        callLogId INTEGER NOT NULL PRIMARY KEY,
                        phoneNumber TEXT NOT NULL,
                        callDateEpochMs INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "callback.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
