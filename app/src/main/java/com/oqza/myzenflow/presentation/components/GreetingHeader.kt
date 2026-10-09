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
import androidx.compose.ui.res.stringResource
import com.oqza.myzenflow.R
import com.oqza.myzenflow.data.models.TimeOfDay
import com.oqza.myzenflow.presentation.theme.ZenSpacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Greeting header for the Home screen: time-of-day greeting, localized date and quote.
 */
@Composable
fun GreetingHeader(
    userName: String? = null,
    motivationalQuote: String,
    timeOfDay: TimeOfDay,
    modifier: Modifier = Modifier
) {
    val greeting = stringResource(
        when (timeOfDay) {
            TimeOfDay.MORNING -> R.string.greeting_morning
            TimeOfDay.AFTERNOON -> R.string.greeting_afternoon
            TimeOfDay.EVENING -> R.string.greeting_evening
            TimeOfDay.NIGHT -> R.string.greeting_night
        }
    )
    val title = if (userName.isNullOrBlank()) greeting else "$greeting, $userName"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ZenSpacing.screen, vertical = ZenSpacing.lg)
    ) {
        Text(
            text = currentDate(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(ZenSpacing.xs))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (motivationalQuote.isNotBlank()) {
            Spacer(modifier = Modifier.height(ZenSpacing.sm))
            Text(
                text = motivationalQuote,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun currentDate(): String =
    LocalDate.now().format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault())
    )
