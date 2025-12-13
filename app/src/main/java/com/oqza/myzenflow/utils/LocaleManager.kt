package com.oqza.myzenflow.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.oqza.myzenflow.data.models.AppLanguage
import java.util.Locale

/**
 * LocaleManager - Manages app localization
 *
 * Features:
 * - Get and set app locale
 * - Handle locale changes with activity recreation
 * - Support for Android versions below and above API 33
 * - Integrate with AppCompatDelegate for per-app language preferences
 */
object LocaleManager {

    /**
     * Get current locale from context
     */
    fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }

    /**
     * Get locale from AppLanguage
     */
    fun getLocale(language: AppLanguage): Locale {
        return Locale(language.code)
    }

    /**
     * Set app locale and recreate activity
     * This method updates the app's language preference using AppCompatDelegate
     * which persists across app restarts
     *
     * @param context Activity context (required for recreation)
     * @param language The language to set
     */
    fun setLocale(context: Context, language: AppLanguage) {
        val locale = getLocale(language)

        // Set using AppCompatDelegate (persists across app restarts)
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)

        // Update configuration for immediate effect
        updateConfiguration(context, locale)

        // Recreate activity if it's an Activity context
        if (context is Activity) {
            context.recreate()
        }
    }

    /**
     * Update configuration for a context
     * This is used for immediate locale changes
     */
    private fun updateConfiguration(context: Context, locale: Locale) {
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            configuration.setLocales(android.os.LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }

        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    /**
     * Apply locale on app startup
     * Should be called in Application.onCreate() or MainActivity.onCreate()
     *
     * @param context Application context
     * @param language The language to apply
     */
    fun applyLocale(context: Context, language: AppLanguage) {
        val locale = getLocale(language)
        updateConfiguration(context, locale)
    }

    /**
     * Get language code from locale
     */
    fun getLanguageCode(locale: Locale): String {
        return locale.language
    }

    /**
     * Get AppLanguage from locale
     */
    fun getAppLanguage(locale: Locale): AppLanguage {
        return AppLanguage.fromCode(locale.language)
    }

    /**
     * Check if a locale matches a language
     */
    fun isLanguage(locale: Locale, language: AppLanguage): Boolean {
        return locale.language == language.code
    }
}
