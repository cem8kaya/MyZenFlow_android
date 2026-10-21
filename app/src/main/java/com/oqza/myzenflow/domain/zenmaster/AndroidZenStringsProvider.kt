package com.oqza.myzenflow.domain.zenmaster

import android.content.Context
import android.content.res.Configuration
import com.oqza.myzenflow.data.models.AppLanguage
import com.oqza.myzenflow.utils.LocaleManager

/**
 * Reads strings in the language chosen in Settings. The application context does not follow the
 * in-app language on every Android version, so a context for that locale is created on demand.
 * "Follow the system" simply uses the application context.
 */
class AndroidZenStringsProvider(private val context: Context) : ZenStringsProvider {

    override fun strings(language: AppLanguage): ZenStrings {
        val localized = localizedContext(language)
        return ZenStrings { id, args ->
            if (args.isEmpty()) localized.getString(id) else localized.getString(id, *args)
        }
    }

    private fun localizedContext(language: AppLanguage): Context {
        if (language == AppLanguage.SYSTEM) return context
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(LocaleManager.getLocale(language))
        return context.createConfigurationContext(configuration)
    }
}
