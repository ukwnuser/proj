package com.callback.app.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    private val testDb = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrate1To2() {
        helper.createDatabase(testDb, 1).apply {
            execSQL(
                """
                INSERT INTO leads (phoneNumber, displayName, isDone, createdAtEpochMs)
                VALUES ('+16502530000', NULL, 0, 1000)
                """.trimIndent(),
            )
            close()
        }

        helper.runMigrationsAndValidate(testDb, 2, true, AppDatabase.MIGRATION_1_2)
    }
}
