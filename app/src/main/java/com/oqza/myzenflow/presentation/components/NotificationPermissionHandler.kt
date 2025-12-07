package com.oqza.myzenflow.presentation.components

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
            title = { Text("Bildirim İzni Gerekli") },
            text = {
                Text(
                    "Pomodoro timer tamamlandığında sizi bilgilendirmek için " +
                            "bildirim izni gereklidir. Lütfen ayarlardan bildirim iznini açın."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        permissionState.launchPermissionRequest()
                    }
                ) {
                    Text("İzin Ver")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        onPermissionDenied()
                    }
                ) {
                    Text("İptal")
                }
            }
        )
    }
}
