package com.oqza.myzenflow.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.oqza.myzenflow.R

/**
 * Asks the user whether they would like to rate the app.
 * [onRate] opens the store listing, [onDecline] is "no thanks", [onDismiss] (tap outside / back) is "not now".
 */
@Composable
fun RatePromptDialog(
    onRate: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(stringResource(R.string.rate_prompt_title)) },
        text = { Text(stringResource(R.string.rate_prompt_message)) },
        confirmButton = {
            TextButton(onClick = onRate) {
                Text(stringResource(R.string.rate_prompt_rate))
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(stringResource(R.string.rate_prompt_decline))
            }
        }
    )
}
