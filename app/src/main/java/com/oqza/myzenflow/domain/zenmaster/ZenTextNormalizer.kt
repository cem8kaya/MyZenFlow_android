package com.oqza.myzenflow.domain.zenmaster

/**
 * Makes user text and keyword lists comparable: lower case, no accents, no punctuation.
 *
 * Turkish needs care. Plain lowercasing turns "I" into "ı" under a Turkish locale and "i" under
 * English, so "INSOMNIA" would depend on the phone's language. Here every i-variant (I, İ, ı, i)
 * folds to "i" and the Turkish letters fold to their ASCII base, for text and keywords alike.
 */
object ZenTextNormalizer {

    fun normalize(text: String): String {
        val out = StringBuilder(text.length)
        var lastWasSpace = true
        for (ch in text) {
            when {
                // Dropped, not turned into a space: "can't" -> "cant", "Meditasyon'a" -> "meditasyona"
                ch == '\'' || ch == '’' || ch == '`' -> Unit
                ch.isLetterOrDigit() -> {
                    out.append(fold(ch))
                    lastWasSpace = false
                }
                !lastWasSpace -> {
                    out.append(' ')
                    lastWasSpace = true
                }
            }
        }
        return out.toString().trimEnd()
    }

    fun tokens(text: String): List<String> =
        normalize(text).let { if (it.isEmpty()) emptyList() else it.split(' ') }

    private fun fold(c: Char): Char = when (c) {
        'İ', 'I', 'ı', 'Î', 'î' -> 'i'
        'Ç', 'ç' -> 'c'
        'Ğ', 'ğ' -> 'g'
        'Ö', 'ö' -> 'o'
        'Ş', 'ş' -> 's'
        'Ü', 'ü', 'Û', 'û' -> 'u'
        'Â', 'â' -> 'a'
        else -> c.lowercaseChar()
    }
}
