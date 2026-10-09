package com.oqza.myzenflow.presentation.viewmodels

import com.oqza.myzenflow.domain.workers.ReminderScheduler
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oqza.myzenflow.data.models.AppLanguage
import com.oqza.myzenflow.data.models.ThemeMode
import com.oqza.myzenflow.data.models.UserPreferences
import com.oqza.myzenflow.data.repository.PreferencesRepository
import com.oqza.myzenflow.utils.LocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen
 * Manages all user preferences and settings
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
    }

    /**
     * Observe user preferences
     */
    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.userPreferences.collectLatest { preferences ->
                _uiState.value = _uiState.value.copy(
                    userPreferences = preferences,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Update language preference
     * This will trigger MainActivity's LaunchedEffect to recreate the activity
     */
    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateLanguage(language)
                // LocaleManager.setLocale is called in MainActivity when the preference changes
                // We just need to update the preference here
                showMessage("Language updated successfully")
            } catch (e: Exception) {
                showError("Failed to update language")
            }
        }
    }

    /**
     * Update haptic feedback setting
     */
    fun updateHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateHapticFeedback(enabled)
            } catch (e: Exception) {
                showError("Failed to update haptic feedback")
            }
        }
    }

    /**
     * Update notifications setting
     */
    fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateNotifications(enabled)
                if (enabled) {
                    showMessage("Notifications enabled")
                } else {
                    showMessage("Notifications disabled")
                }
            } catch (e: Exception) {
                showError("Failed to update notifications")
            }
        }
    }

    /**
     * Update daily reminder
     */
    fun updateDailyReminder(enabled: Boolean, time: String = "09:00") {
        viewModelScope.launch {
            try {
                preferencesRepository.updateDailyReminder(enabled, time)
                if (enabled) reminderScheduler.schedule(time, replace = true) else reminderScheduler.cancel()
                if (enabled) {
                    showMessage("Daily reminder set for $time")
                } else {
                    showMessage("Daily reminder disabled")
                }
            } catch (e: Exception) {
                showError("Failed to update daily reminder")
            }
        }
    }

    /**
     * Update sound enabled setting
     */
    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val prefs = _uiState.value.userPreferences
                preferencesRepository.updateSoundSettings(
                    soundEnabled = enabled,
                    volume = prefs.soundVolume,
                    backgroundMusicEnabled = prefs.backgroundMusicEnabled,
                    backgroundMusicType = prefs.backgroundMusicType
                )
            } catch (e: Exception) {
                showError("Failed to update sound settings")
            }
        }
    }

    /**
     * Update sound volume
     */
    fun updateSoundVolume(volume: Float) {
        viewModelScope.launch {
            try {
                val prefs = _uiState.value.userPreferences
                preferencesRepository.updateSoundSettings(
                    soundEnabled = prefs.soundEnabled,
                    volume = volume,
                    backgroundMusicEnabled = prefs.backgroundMusicEnabled,
                    backgroundMusicType = prefs.backgroundMusicType
                )
            } catch (e: Exception) {
                showError("Failed to update volume")
            }
        }
    }

    /**
     * Update background music setting
     */
    fun updateBackgroundMusic(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val prefs = _uiState.value.userPreferences
                preferencesRepository.updateSoundSettings(
                    soundEnabled = prefs.soundEnabled,
                    volume = prefs.soundVolume,
                    backgroundMusicEnabled = enabled,
                    backgroundMusicType = prefs.backgroundMusicType
                )
            } catch (e: Exception) {
                showError("Failed to update background music")
            }
        }
    }

    /**
     * Update dark mode setting
     */
    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateDynamicColor(enabled)
            } catch (e: Exception) {
                showError("Failed to update theme")
            }
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateThemeMode(mode)
            } catch (e: Exception) {
                showError("Failed to update theme")
            }
        }
    }

    /**
     * Navigate to premium/paywall (handled by navigation)
     */
    fun navigateToPremium() {
        _uiState.value = _uiState.value.copy(
            navigateToPremium = true
        )
    }

    /**
     * Clear premium navigation flag
     */
    fun clearPremiumNavigation() {
        _uiState.value = _uiState.value.copy(
            navigateToPremium = false
        )
    }

    /**
     * Restore purchases (check with billing client)
     */
    fun restorePurchases() {
        viewModelScope.launch {
            try {
                // TODO: Integrate with BillingClient when ready
                // For now, just check current status from preferences
                val isPremium = _uiState.value.userPreferences.isPremiumUnlocked
                if (isPremium) {
                    showMessage("Premium is already active")
                } else {
                    showMessage("No purchases to restore")
                }
            } catch (e: Exception) {
                showError("Failed to restore purchases")
            }
        }
    }

    /**
     * Show time picker dialog
     */
    fun showTimePicker() {
        _uiState.value = _uiState.value.copy(
            showTimePickerDialog = true
        )
    }

    /**
     * Hide time picker dialog
     */
    fun hideTimePicker() {
        _uiState.value = _uiState.value.copy(
            showTimePickerDialog = false
        )
    }

    /**
     * Show language selector dialog
     */
    fun showLanguageSelector() {
        _uiState.value = _uiState.value.copy(
            showLanguageSelectorDialog = true
        )
    }

    /**
     * Hide language selector dialog
     */
    fun hideLanguageSelector() {
        _uiState.value = _uiState.value.copy(
            showLanguageSelectorDialog = false
        )
    }

    /**
     * Show success message
     */
    private fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(
            snackbarMessage = message
        )
    }

    /**
     * Show error message
     */
    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            snackbarMessage = message
        )
    }

    /**
     * Clear snackbar message
     */
    fun clearSnackbarMessage() {
        _uiState.value = _uiState.value.copy(
            snackbarMessage = null
        )
    }
}

/**
 * UI state for Settings screen
 */
data class SettingsUiState(
    val isLoading: Boolean = true,
    val userPreferences: UserPreferences = UserPreferences(),
    val showTimePickerDialog: Boolean = false,
    val showLanguageSelectorDialog: Boolean = false,
    val navigateToPremium: Boolean = false,
    val snackbarMessage: String? = null
)
