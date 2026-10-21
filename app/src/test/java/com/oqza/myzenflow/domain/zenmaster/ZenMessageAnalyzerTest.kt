package com.oqza.myzenflow.domain.zenmaster

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZenMessageAnalyzerTest {

    private fun intent(text: String) = ZenMessageAnalyzer.classifyIntent(text)
    private fun sentiment(text: String) = ZenMessageAnalyzer.analyzeSentiment(text)

    private fun assertIntents(expected: ZenIntent, vararg texts: String) =
        texts.forEach { assertEquals("\"$it\"", expected, intent(it)) }

    // ---------------------------------------------------------------- the five starter prompts, in both languages

    @Test
    fun `starter prompts land on the intended intent`() {
        assertIntents(ZenIntent.MEDITATION, "How do I start meditating?", "Meditasyona nasıl başlarım?")
        assertIntents(ZenIntent.STRESS, "I'm mentally tired, help me", "Zihinsel olarak yorgunum, yardım et")
        assertIntents(ZenIntent.FOCUS, "How can I improve my focus?", "Odaklanmamı nasıl artırabilirim?")
        assertIntents(ZenIntent.SLEEP, "I can't sleep well at night", "Gece rahat uyuyamıyorum")
        assertIntents(ZenIntent.MOTIVATION, "I need motivation today", "Bugün için motivasyon lazım")
    }

    // ---------------------------------------------------------------- intents

    @Test
    fun `stress`() {
        assertIntents(ZenIntent.STRESS, "Çok stresli hissediyorum", "I'm so worried and anxious", "STRESS!", "I need to calm down", "Kaygılarımı nasıl kontrol ederim?")
    }

    @Test
    fun `needing to unwind is stress and a racing mind at night is sleep`() {
        assertIntents(ZenIntent.STRESS, "Hemen rahatlamam lazım", "Need immediate relief", "I want to relax")
        assertIntents(ZenIntent.SLEEP, "Zihnim durmuyor gece", "Mind won't stop at night")
    }

    @Test
    fun `focus`() {
        assertIntents(ZenIntent.FOCUS, "Pomodoro tekniği nedir?", "Konsantre olamıyorum", "I'm so distracted", "Dikkatim çok dağınık")
    }

    @Test
    fun `sleep`() {
        assertIntents(ZenIntent.SLEEP, "INSOMNIA", "İNSOMNİA", "Uyumakta zorlanıyorum", "I feel exhausted", "Kabus gördüm")
    }

    @Test
    fun `breathing`() {
        assertIntents(ZenIntent.BREATHING, "4-7-8 nefes tekniği göster", "Show me box breathing", "Derin nefes almak istiyorum", "Calming breath please")
    }

    @Test
    fun `progress`() {
        assertIntents(ZenIntent.PROGRESS, "What are my stats?", "Rozetlerimi göster", "Serim kaç gün?", "Show my achievements")
    }

    @Test
    fun `garden counts as motivation`() {
        assertIntents(ZenIntent.MOTIVATION, "Zen bahçem nasıl?", "How is my garden?")
    }

    @Test
    fun `unrelated or empty text is general`() {
        assertIntents(ZenIntent.GENERAL, "Merhaba", "hello there", "", "   ", "🙂")
    }

    // ---------------------------------------------------------------- bugs the iOS version has

    @Test
    fun `short Turkish word iş does not fire inside kişi or işte`() {
        assertIntents(ZenIntent.GENERAL, "kişiler işte böyle", "Bu kişi kim?")
    }

    @Test
    fun `improve does not mean progress`() {
        assertEquals(ZenIntent.FOCUS, intent("How can I improve my focus?"))
    }

    @Test
    fun `a keyword counts once even when listed in both languages`() {
        // "stress" starts with the Turkish stem "stres"; it must not outscore a real two-word phrase
        assertEquals(ZenIntent.STRESS, intent("stress"))
        assertEquals(ZenIntent.STRESS, intent("mentally tired"))
    }

    @Test
    fun `equal scores go to the earlier intent`() {
        // "calming" hits meditation (calm), "breath" hits breathing; breathing is declared first
        assertEquals(ZenIntent.BREATHING, intent("calming breath"))
    }

    // ---------------------------------------------------------------- sentiment

    @Test
    fun `negative`() {
        listOf("Çok stresli hissediyorum", "Gece rahat uyuyamıyorum", "I can't sleep well at night",
            "Konsantre olamıyorum", "I feel terrible", "Zihinsel olarak yorgunum", "Having trouble sleeping").forEach {
            assertEquals("\"$it\"", ZenSentiment.NEGATIVE, sentiment(it))
        }
    }

    @Test
    fun `positive`() {
        listOf("Teşekkürler, harika!", "I'm feeling great today", "Çok mutluyum", "thank you").forEach {
            assertEquals("\"$it\"", ZenSentiment.POSITIVE, sentiment(it))
        }
    }

    @Test
    fun `neutral`() {
        listOf("Meditasyona nasıl başlarım?", "How do I start meditating?", "Bugün için motivasyon lazım", "", "Merhaba").forEach {
            assertEquals("\"$it\"", ZenSentiment.NEUTRAL, sentiment(it))
        }
    }

    @Test
    fun `negation flips the word`() {
        assertEquals(ZenSentiment.NEGATIVE, sentiment("I'm not happy"))
        assertEquals(ZenSentiment.NEGATIVE, sentiment("iyi değilim"))
        assertEquals(ZenSentiment.POSITIVE, sentiment("Stresli değilim"))
        assertEquals(ZenSentiment.POSITIVE, sentiment("I am not stressed"))
    }

    @Test
    fun `mixed feelings cancel out`() {
        assertEquals(ZenSentiment.NEUTRAL, sentiment("Bugün iyi ama biraz yorgunum"))
    }

    // ---------------------------------------------------------------- crisis

    @Test
    fun `crisis phrases are caught in both languages`() {
        listOf(
            "I want to die", "I don't want to live anymore", "thinking about suicide", "I might kill myself",
            "I want to hurt myself", "I want to end my life", "Self-harm thoughts",
            "Ölmek istiyorum", "Yaşamak istemiyorum", "İNTİHAR etmeyi düşünüyorum", "Kendime zarar vermek istiyorum",
            "Hayatıma son vermek istiyorum", "canıma kıyacağım", "canıma kıymak istiyorum", "hayatımı bitirmek istiyorum"
        ).forEach { assertTrue("\"$it\"", ZenMessageAnalyzer.isCrisis(it)) }
    }

    @Test
    fun `everyday phrases that share words are not crisis`() {
        listOf(
            "Hayatıma son derece memnunum", "I want to dive into meditation", "I don't want to be late",
            "How do I start meditating?", "Gece rahat uyuyamıyorum", "stres", "", "ölçüm yapmak istiyorum"
        ).forEach { assertFalse("\"$it\"", ZenMessageAnalyzer.isCrisis(it)) }
    }

    @Test
    fun `analyze reports crisis instead of an intent`() {
        val result = ZenMessageAnalyzer.analyze("I want to die, I can't sleep")
        assertTrue(result.isCrisis)
        assertEquals(ZenIntent.GENERAL, result.intent)
        assertEquals(ZenSentiment.NEGATIVE, result.sentiment)
    }

    @Test
    fun `analyze combines intent and sentiment`() {
        val result = ZenMessageAnalyzer.analyze("Çok stresli hissediyorum")
        assertEquals(ZenAnalysis(ZenIntent.STRESS, ZenSentiment.NEGATIVE, isCrisis = false), result)
    }
}
