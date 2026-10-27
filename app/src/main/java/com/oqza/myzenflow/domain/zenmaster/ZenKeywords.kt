package com.oqza.myzenflow.domain.zenmaster

/**
 * Keyword lists for the rule-based analyzer, Turkish and English together. People type in either
 * language whatever the app language is, so both are always searched.
 *
 * Matching rules (see [ZenMessageAnalyzer]): a keyword matches a word that starts with it, so
 * "stres" also catches "stresli" and "stressed" is caught by "stress". Keywords of three letters or
 * fewer must match the whole word, which keeps short stems from firing inside unrelated words.
 * Several words in one entry ("mentally tired") are a phrase and count double.
 *
 * Lists are written naturally ("kaygı") and normalized once when loaded.
 */
internal object ZenKeywords {

    val intents: Map<ZenIntent, List<List<String>>> = mapOf(
        ZenIntent.STRESS to compile(
            // tr
            "stres", "kaygı", "endişe", "gergin", "huzursuz", "sinirli", "sinirlen", "tedirgin", "panik", "korku",
            "bunal", "daral", "sıkıntı", "tüken", "zihinsel olarak yorgun", "zihinsel yorgun", "zihnim yorgun",
            "sakinleşmem lazım", "rahatla",
            // en
            "stress", "anxi", "worr", "nervous", "panic", "fear", "scared", "overwhelm", "tension", "upset",
            "angry", "anger", "relax", "relief", "burnout", "burned out", "burnt out", "mentally tired", "mental fatigue", "calm down"
        ),
        ZenIntent.FOCUS to compile(
            "odak", "dikkat", "konsantr", "dağınık", "verim", "pomodoro", "çalış", "ders",
            "focus", "attention", "concentrat", "distract", "productiv", "pomodoro", "efficien", "study", "deep work"
        ),
        ZenIntent.SLEEP to compile(
            "uyku", "uyuy", "uyuma", "uyumak", "uyudu", "uyuya", "yorgun", "bitkin", "dinlen", "kabus",
            "yatamıyor", "yatmak", "yatağ", "zihnim durmuyor", "zihinim durmuyor",
            "sleep", "insomnia", "tired", "exhaust", "nap", "drows", "bedtime", "dream", "awake", "nightmare", "mind wont stop", "mind is racing"
        ),
        ZenIntent.BREATHING to compile(
            "nefes", "soluk", "box breathing", "4-7-8",
            "breath", "inhale", "exhale", "lung"
        ),
        ZenIntent.MOTIVATION to compile(
            "motivasyon", "üşen", "enerji", "güç", "ilham", "cesaret", "isteksiz", "tembel", "bahçe", "ağaç",
            "büyüme", "çiçek", "bitki", "heves",
            "motivat", "unmotivat", "inspir", "energy", "courage", "lazy", "procrastinat", "garden", "tree", "grow", "plant"
        ),
        ZenIntent.MEDITATION to compile(
            "meditasyon", "mindfulness", "farkındalık", "sakinleş", "huzur", "başlangıç",
            "meditat", "mindful", "awareness", "calm", "peace", "beginner"
        ),
        ZenIntent.PROGRESS to compile(
            "ilerleme", "gelişim", "istatistik", "rozet", "başarı", "serim", "skor", "puan", "toplam",
            "progress", "improvement", "stats", "statistic", "badge", "achiev", "streak", "score", "how am i doing"
        ),
    )

    val positive: List<List<String>> = compile(
        "harika", "güzel", "teşekkür", "mükemmel", "mutlu", "iyi", "iyiyim", "iyileş", "süper", "sevindim",
        "keyifli", "rahatladım", "memnun", "huzurlu", "minnettar",
        "great", "good", "happy", "thank", "wonderful", "awesome", "amazing", "love", "grateful", "glad",
        "excellent", "perfect", "nice", "fantastic", "relieved"
    )

    val negative: List<List<String>> = compile(
        "kötü", "üzgün", "stres", "kaygı", "korku", "panik", "yorgun", "bitkin", "berbat", "sinirli", "huzursuz",
        "ağır", "bunal", "daral", "sıkıntı", "endişe", "gergin", "mutsuz", "yalnız", "çaresiz", "umutsuz", "tüken",
        "zorlan", "tedirgin", "bıktım", "sıkıl",
        "bad", "terrible", "awful", "sad", "stress", "anxi", "worr", "fear", "panic", "tired", "exhaust", "angry",
        "upset", "trouble", "struggl", "overwhelm", "nervous", "scared", "hopeless", "lonely", "depress", "miserable", "hate"
    )

    /** Words that flip a nearby positive/negative word ("not good", "iyi değilim"). */
    val negators: Set<String> = setOf("not", "no", "never", "isnt", "wasnt", "dont", "aint", "hic")

    /** "I can't ..." in English; Turkish "-amıyorum" forms are handled by suffix in the analyzer. */
    val cannotWords: Set<String> = setOf("cant", "cannot", "couldnt")

    /** Phrases that mean the user may be in danger. Matched from the start of a word. */
    val crisis: List<List<String>> = compile(
        "suicide", "suicidal", "kill myself", "end my life", "want to die", "wanna die", "dont want to live",
        "self harm", "hurt myself", "harm myself", "better off dead", "no reason to live",
        "intihar", "kendimi öldür", "kendime zarar", "ölmek istiyorum", "yaşamak istemiyorum",
        "hayatıma son verm", "hayatıma son verec", "hayatıma son veriyor", "hayatımı bitir",
        // "kıy" is three letters and would need an exact match, so list the verb forms
        "canıma kıyac", "canıma kıymak", "canıma kıyar", "canıma kıyd"
    )

    private fun compile(vararg phrases: String): List<List<String>> {
        val all = phrases.map { ZenTextNormalizer.tokens(it) }.filter { it.isNotEmpty() }.distinct()
        val stems = all.filter { it.size == 1 && it[0].length > 3 }.map { it[0] }
        // A word that already starts with another listed stem ("stress" after "stres") would score
        // the same message twice, so only the shortest stem stays.
        return all.filterNot { p -> p.size == 1 && stems.any { it != p[0] && p[0].startsWith(it) } }
    }
}
