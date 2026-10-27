package com.oqza.myzenflow.domain.zenmaster

import com.oqza.myzenflow.data.models.AppLanguage
import com.oqza.myzenflow.data.models.UserStats

/** The user's practice numbers, for the personal note in a reply. Null when unavailable. */
fun interface ZenUserStatsSource {
    suspend fun current(): UserStats?
}

/** The language picked in Settings. */
fun interface ZenLanguageSource {
    suspend fun current(): AppLanguage
}
