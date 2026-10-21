package com.oqza.myzenflow.domain.zenmaster

/** The result of reading one user message. */
data class ZenAnalysis(
    val intent: ZenIntent,
    val sentiment: ZenSentiment,
    /** The message suggests the user may be in danger; answer with the crisis reply, nothing else. */
    val isCrisis: Boolean = false
)

/**
 * Reads a message: what it is about, how it feels, and whether the person may be in danger.
 * Rule based and offline, like the iOS Zen Coach, but it scores every intent instead of taking the
 * first keyword hit, and matches word starts instead of any substring.
 */
object ZenMessageAnalyzer {

    fun analyze(text: String): ZenAnalysis {
        val tokens = ZenTextNormalizer.tokens(text)
        if (isCrisis(tokens)) {
            return ZenAnalysis(ZenIntent.GENERAL, ZenSentiment.NEGATIVE, isCrisis = true)
        }
        return ZenAnalysis(classifyIntent(tokens), analyzeSentiment(tokens))
    }

    fun classifyIntent(text: String): ZenIntent = classifyIntent(ZenTextNormalizer.tokens(text))

    fun analyzeSentiment(text: String): ZenSentiment = analyzeSentiment(ZenTextNormalizer.tokens(text))

    fun isCrisis(text: String): Boolean = isCrisis(ZenTextNormalizer.tokens(text))

    // ---------------------------------------------------------------- intent

    private fun classifyIntent(tokens: List<String>): ZenIntent {
        if (tokens.isEmpty()) return ZenIntent.GENERAL
        var best = ZenIntent.GENERAL
        var bestScore = 0
        // Iterating in declaration order with a strict ">" makes the earlier intent win ties
        for (intent in ZenIntent.entries) {
            val keywords = ZenKeywords.intents[intent] ?: continue
            val score = keywords.sumOf { phrase -> if (containsPhrase(tokens, phrase)) phrase.size.coerceAtMost(2) else 0 }
            if (score > bestScore) {
                best = intent
                bestScore = score
            }
        }
        return best
    }

    // ---------------------------------------------------------------- sentiment

    private fun analyzeSentiment(tokens: List<String>): ZenSentiment {
        var positive = 0
        var negative = 0
        tokens.forEachIndexed { i, token ->
            val inability = token in ZenKeywords.cannotWords ||
                token.contains("amiyor") || token.contains("emiyor") // olamıyorum, uyuyamıyorum, yapamıyorum
            val polarity = when {
                inability -> -1
                ZenKeywords.positive.any { startsWithKeyword(token, it[0]) } -> 1
                ZenKeywords.negative.any { startsWithKeyword(token, it[0]) } -> -1
                else -> 0
            }
            if (polarity == 0) return@forEachIndexed
            val effective = if (!inability && isNegated(tokens, i)) -polarity else polarity
            if (effective > 0) positive++ else negative++
        }
        return when {
            positive > negative -> ZenSentiment.POSITIVE
            negative > positive -> ZenSentiment.NEGATIVE
            else -> ZenSentiment.NEUTRAL
        }
    }

    private fun isNegated(tokens: List<String>, i: Int): Boolean {
        val previous = tokens.getOrNull(i - 1)
        val next = tokens.getOrNull(i + 1)
        return (previous != null && previous in ZenKeywords.negators) ||
            (next != null && (next.startsWith("degil") || next.contains("miyor") || next.contains("muyor")))
    }

    // ---------------------------------------------------------------- crisis

    private fun isCrisis(tokens: List<String>): Boolean =
        tokens.isNotEmpty() && ZenKeywords.crisis.any { containsPhrase(tokens, it) }

    // ---------------------------------------------------------------- matching

    /** Whether the words of [phrase] appear next to each other in [tokens], each matching a word start. */
    private fun containsPhrase(tokens: List<String>, phrase: List<String>): Boolean {
        if (phrase.size > tokens.size) return false
        for (start in 0..tokens.size - phrase.size) {
            if (phrase.indices.all { startsWithKeyword(tokens[start + it], phrase[it]) }) return true
        }
        return false
    }

    /** Short keywords (3 letters or fewer) match whole words only; longer ones match a word start. */
    private fun startsWithKeyword(token: String, keyword: String): Boolean =
        if (keyword.length <= 3) token == keyword else token.startsWith(keyword)
}
