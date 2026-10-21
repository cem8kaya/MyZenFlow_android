package com.oqza.myzenflow.domain.zenmaster

import androidx.annotation.StringRes

/** Resolves string resources, so the reply logic can run in plain JVM tests. */
fun interface ZenStrings {
    fun get(@StringRes id: Int, vararg args: Any): String
}
