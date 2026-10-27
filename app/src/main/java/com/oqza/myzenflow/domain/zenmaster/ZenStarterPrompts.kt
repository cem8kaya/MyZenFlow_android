package com.oqza.myzenflow.domain.zenmaster

import androidx.annotation.StringRes
import com.oqza.myzenflow.R

/** A tappable first question. The emoji is kept apart so only the words are sent. */
data class ZenStarterPrompt(val emoji: String, @StringRes val textRes: Int)

object ZenStarterPrompts {
    val all: List<ZenStarterPrompt> = listOf(
        ZenStarterPrompt("🌸", R.string.zm_prompt_0),
        ZenStarterPrompt("💭", R.string.zm_prompt_1),
        ZenStarterPrompt("⚡", R.string.zm_prompt_2),
        ZenStarterPrompt("😴", R.string.zm_prompt_3),
        ZenStarterPrompt("🎯", R.string.zm_prompt_4),
    )
}
