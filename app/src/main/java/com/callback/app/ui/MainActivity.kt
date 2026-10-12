package com.callback.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.callback.app.data.AppDatabase
import com.callback.app.ui.inbox.InboxScreenFromState
import com.callback.app.ui.onboarding.OnboardingScreen
import com.callback.app.ui.theme.CallBackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.getInstance(this)

        setContent {
            CallBackTheme {
                var showOnboarding by rememberSaveable { mutableStateOf(true) }
                if (showOnboarding) {
                    OnboardingScreen(onContinue = { showOnboarding = false })
                } else {
                    InboxScreenFromState(
                        newLeadsFlow = database.leadDao().observeNewLeads(),
                        doneLeadsFlow = database.leadDao().observeDoneLeads(),
                        database = database,
                    )
                }
            }
        }
    }
}
