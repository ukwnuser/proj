package com.callback.app.ui.onboarding

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.callback.app.R

private val REQUIRED_PERMISSIONS = listOf(
    Manifest.permission.READ_CALL_LOG,
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.SEND_SMS,
    Manifest.permission.RECEIVE_SMS,
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.POST_NOTIFICATIONS,
)

/** Onboarding screen that handles runtime permissions. */
@Composable
fun OnboardingScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasPermissions by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
        if (!hasPermissions) {
            showRationale = permissions.values.any { !it }
        }
    }

    LaunchedEffect(Unit) {
        hasPermissions = REQUIRED_PERMISSIONS.all {
            ActivityCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.onboarding_body),
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
            style = MaterialTheme.typography.bodyLarge,
        )

        if (showRationale) {
            Text(
                text = stringResource(R.string.permissions_denied),
                modifier = Modifier.padding(bottom = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
            ) {
                Text(stringResource(R.string.settings_label))
            }
        } else if (!hasPermissions) {
            Button(
                onClick = { permissionLauncher.launch(REQUIRED_PERMISSIONS.toTypedArray()) },
            ) {
                Text(stringResource(R.string.request_permissions_label))
            }
        } else {
            Button(onClick = onContinue) {
                Text(stringResource(R.string.continue_label))
            }
        }
    }
}
