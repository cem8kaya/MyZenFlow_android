package com.oqza.myzenflow.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Greeting header component for Home screen
 * Displays greeting message, motivational quote, and current date
 */
@Composable
fun GreetingHeader(
    userName: String? = null,
    motivationalQuote: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Greeting
        Text(
            text = getGreeting(userName),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Date
        Text(
            text = getCurrentDate(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Motivational quote
        Text(
            text = motivationalQuote,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Get greeting message based on time of day
 */
private fun getGreeting(userName: String?): String {
    val hour = LocalDateTime.now().hour
    val greeting = when (hour) {
        in 5..11 -> "Günaydın"
        in 12..17 -> "İyi günler"
        in 18..21 -> "İyi akşamlar"
        else -> "İyi geceler"
    }

    return if (userName != null) {
        "$greeting, $userName"
    } else {
        greeting
    }
}

/**
 * Get current date formatted in Turkish
 */
private fun getCurrentDate(): String {
    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", Locale("tr"))
    return now.format(formatter)
}
