package com.oqza.myzenflow.domain.zenmaster

import com.oqza.myzenflow.data.models.AppLanguage

/**
 * Gives strings in the language the user picked in Settings. That is not always the language of the
 * application context: on older Android versions only the activity follows the in-app language.
 */
fun interface ZenStringsProvider {
    fun strings(language: AppLanguage): ZenStrings
}
