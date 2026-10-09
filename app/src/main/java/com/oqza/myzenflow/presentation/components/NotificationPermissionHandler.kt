package com.oqza.myzenflow.presentation.components

import com.oqza.myzenflow.R
import androidx.compose.ui.res.stringResource
import android.Manifest
import android.os.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Handles notification permission request for Android 13+
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionHandler(
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {}
) {
    // Only request permission on Android 13+
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        // Permission not required, consider it granted
        LaunchedEffect(Unit) {
            onPermissionGranted()
        }
        return
    }

    val permissionState = rememberPermissionState(
        permission = Manifest.permission.POST_NOTIFICATIONS
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    var showRationale by remember { mutableStateOf(false) }

    LaunchedEffect(permissionState.status) {
        if (!permissionState.status.isGranted) {
            if (permissionState.status.shouldShowRationale) {
                showRationale = true
            } else {
                permissionState.launchPermissionRequest()
            }
        } else {
            onPermissionGranted()
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = {
                showRationale = false
                onPermissionDenied()
            },
            title = { Text(stringResource(R.string.notif_perm_title)) },
            text = {
                Text(stringResource(R.string.notif_perm_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        permissionState.launchPermissionRequest()
                    }
                ) {
                    Text(stringResource(R.string.notif_perm_allow))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        onPermissionDenied()
                    }
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }
}

/**
 * Returns a function that asks for the notification permission when it is still missing
 * (Android 13+). Call it right when the user turns on something that needs notifications
 * (reminders, timer alerts), so the request has a clear reason. [onDenied] runs when the user says no.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberNotificationPermissionRequester(onDenied: () -> Unit = {}): () -> Unit {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return remember { {} }
    }
    val state = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) { granted ->
        if (!granted) onDenied()
    }
    return remember(state) {
        { if (!state.status.isGranted) state.launchPermissionRequest() }
    }
}
