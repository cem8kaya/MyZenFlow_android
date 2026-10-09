package com.oqza.myzenflow.presentation.screens

import android.content.Intent
import android.os.Build
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oqza.myzenflow.BuildConfig
import com.oqza.myzenflow.R
import com.oqza.myzenflow.data.models.ThemeMode
import com.oqza.myzenflow.presentation.components.SettingThemeModeItem
import com.oqza.myzenflow.data.models.AppLanguage
import com.oqza.myzenflow.presentation.components.PremiumCard
import com.oqza.myzenflow.presentation.components.PremiumStatusCard
import com.oqza.myzenflow.presentation.components.SettingItem
import com.oqza.myzenflow.presentation.components.SettingSection
import com.oqza.myzenflow.presentation.components.SettingSliderItem
import com.oqza.myzenflow.presentation.components.SettingToggleItem
import com.oqza.myzenflow.presentation.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }

    // Handle navigation to premium (this would be implemented with actual navigation)
    LaunchedEffect(uiState.navigateToPremium) {
        if (uiState.navigateToPremium) {
            // TODO: Navigate to paywall screen when implemented
            // navController.navigate(Screen.Paywall.route)
            viewModel.clearPremiumNavigation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.loading))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // SECTION 1: Premium
                item {
                    SettingSection(title = stringResource(R.string.section_premium)) {
                        if (!uiState.userPreferences.isPremiumUnlocked) {
                            PremiumCard(
                                onUpgradeClick = { viewModel.navigateToPremium() }
                            )
                        } else {
                            PremiumStatusCard(
                                onRestorePurchases = { viewModel.restorePurchases() }
                            )
                        }
                    }
                }

                // SECTION 2: General
                item {
                    SettingSection(title = stringResource(R.string.section_general)) {
                        Column {
                            SettingItem(
                                title = stringResource(R.string.language_setting),
                                subtitle = uiState.userPreferences.language.displayName,
                                icon = Icons.Default.Language,
                                onClick = { viewModel.showLanguageSelector() },
                                trailing = {
                                    Icon(
                                        imageVector = Icons.Outlined.AccessTime,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )

                            SettingThemeModeItem(
                                title = stringResource(R.string.theme_setting),
                                selected = uiState.userPreferences.themeMode,
                                labels = mapOf(
                                    ThemeMode.SYSTEM to stringResource(R.string.theme_system),
                                    ThemeMode.LIGHT to stringResource(R.string.theme_light),
                                    ThemeMode.DARK to stringResource(R.string.theme_dark)
                                ),
                                onSelected = { viewModel.updateThemeMode(it) }
                            )

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                SettingToggleItem(
                                    title = stringResource(R.string.dynamic_color),
                                    subtitle = stringResource(R.string.dynamic_color_subtitle),
                                    checked = uiState.userPreferences.dynamicColorEnabled,
                                    onCheckedChange = { viewModel.updateDynamicColor(it) },
                                    icon = Icons.Default.Brightness4
                                )
                            }
                        }
                    }
                }

                // SECTION 3: Notifications
                item {
                    SettingSection(title = stringResource(R.string.section_notifications)) {
                        Column {
                            SettingToggleItem(
                                title = stringResource(R.string.notifications),
                                subtitle = stringResource(R.string.notifications_subtitle),
                                checked = uiState.userPreferences.notificationsEnabled,
                                onCheckedChange = { viewModel.updateNotifications(it) },
                                icon = Icons.Default.Notifications
                            )

                            SettingToggleItem(
                                title = stringResource(R.string.daily_reminder),
                                subtitle = if (uiState.userPreferences.dailyReminderEnabled) {
                                    stringResource(R.string.daily_reminder_time, uiState.userPreferences.dailyReminderTime)
                                } else {
                                    stringResource(R.string.daily_reminder_subtitle)
                                },
                                checked = uiState.userPreferences.dailyReminderEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        viewModel.showTimePicker()
                                    } else {
                                        viewModel.updateDailyReminder(false)
                                    }
                                },
                                icon = Icons.Outlined.AccessTime,
                                enabled = uiState.userPreferences.notificationsEnabled
                            )

                            if (uiState.userPreferences.dailyReminderEnabled && uiState.userPreferences.notificationsEnabled) {
                                SettingItem(
                                    title = stringResource(R.string.reminder_time),
                                    subtitle = uiState.userPreferences.dailyReminderTime,
                                    icon = Icons.Outlined.AccessTime,
                                    onClick = { viewModel.showTimePicker() }
                                )
                            }
                        }
                    }
                }

                // SECTION 4: Sound & Haptics
                item {
                    SettingSection(title = stringResource(R.string.section_sound_haptics)) {
                        Column {
                            SettingToggleItem(
                                title = stringResource(R.string.haptic_feedback),
                                subtitle = stringResource(R.string.haptic_feedback_subtitle),
                                checked = uiState.userPreferences.hapticFeedbackEnabled,
                                onCheckedChange = { viewModel.updateHapticFeedback(it) },
                                icon = Icons.Default.TouchApp
                            )

                            SettingToggleItem(
                                title = stringResource(R.string.sound_settings_title),
                                subtitle = stringResource(R.string.sound_settings_subtitle),
                                checked = uiState.userPreferences.soundEnabled,
                                onCheckedChange = { viewModel.updateSoundEnabled(it) },
                                icon = Icons.Default.VolumeUp
                            )

                            if (uiState.userPreferences.soundEnabled) {
                                SettingSliderItem(
                                    title = stringResource(R.string.sound_volume),
                                    subtitle = stringResource(R.string.sound_volume_subtitle),
                                    value = uiState.userPreferences.soundVolume,
                                    onValueChange = { viewModel.updateSoundVolume(it) },
                                    icon = Icons.Default.VolumeUp,
                                    valueLabel = "${(uiState.userPreferences.soundVolume * 100).toInt()}%"
                                )

                                SettingToggleItem(
                                    title = stringResource(R.string.background_music),
                                    subtitle = stringResource(R.string.background_music_subtitle),
                                    checked = uiState.userPreferences.backgroundMusicEnabled,
                                    onCheckedChange = { viewModel.updateBackgroundMusic(it) },
                                    icon = Icons.Default.MusicNote
                                )
                            }
                        }
                    }
                }

                // SECTION 5: About
                item {
                    SettingSection(title = stringResource(R.string.section_about)) {
                        Column {
                            SettingItem(
                                title = stringResource(R.string.about_version),
                                subtitle = BuildConfig.VERSION_NAME,
                                icon = Icons.Default.Info,
                                showDivider = true
                            )

                            SettingItem(
                                title = stringResource(R.string.privacy_policy),
                                icon = Icons.Default.Policy,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myzenflow.com/privacy"))
                                    context.startActivity(intent)
                                },
                                showDivider = true
                            )

                            SettingItem(
                                title = stringResource(R.string.terms_service),
                                icon = Icons.Outlined.Article,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myzenflow.com/terms"))
                                    context.startActivity(intent)
                                },
                                showDivider = true
                            )

                            SettingItem(
                                title = stringResource(R.string.support_email),
                                subtitle = "support@myzenflow.com",
                                icon = Icons.Default.Email,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:support@myzenflow.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "ZenFlow Support")
                                    }
                                    context.startActivity(intent)
                                },
                                showDivider = true
                            )

                            SettingItem(
                                title = stringResource(R.string.rate_app),
                                icon = Icons.Default.Star,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                                    context.startActivity(intent)
                                },
                                showDivider = false
                            )
                        }
                    }
                }
            }
        }
    }

    // Language Selector Dialog
    if (uiState.showLanguageSelectorDialog) {
        LanguageSelectorDialog(
            currentLanguage = uiState.userPreferences.language,
            onLanguageSelected = { language ->
                viewModel.updateLanguage(language)
                viewModel.hideLanguageSelector()
            },
            onDismiss = { viewModel.hideLanguageSelector() }
        )
    }

    // Time Picker Dialog
    if (uiState.showTimePickerDialog) {
        TimePickerDialog(
            currentTime = uiState.userPreferences.dailyReminderTime,
            onTimeSelected = { time ->
                viewModel.updateDailyReminder(true, time)
                viewModel.hideTimePicker()
            },
            onDismiss = { viewModel.hideTimePicker() }
        )
    }
}

/**
 * Language selector dialog
 */
@Composable
private fun LanguageSelectorDialog(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.language_select),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                AppLanguage.values().forEach { language ->
                    LanguageOption(
                        language = language,
                        isSelected = language == currentLanguage,
                        onClick = { onLanguageSelected(language) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}

/**
 * Language option item
 */
@Composable
private fun LanguageOption(
    language: AppLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = language.displayName,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Time picker dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    currentTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val timeParts = currentTime.split(":")
    val initialHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
    val initialMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.reminder_time),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            TimePicker(
                state = timePickerState
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hour = timePickerState.hour.toString().padStart(2, '0')
                    val minute = timePickerState.minute.toString().padStart(2, '0')
                    onTimeSelected("$hour:$minute")
                }
            ) {
                Text(stringResource(R.string.button_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}
