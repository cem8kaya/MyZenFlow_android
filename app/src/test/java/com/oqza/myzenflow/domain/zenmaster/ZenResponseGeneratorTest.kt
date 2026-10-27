package com.oqza.myzenflow.domain.zenmaster

import com.oqza.myzenflow.R
import com.oqza.myzenflow.data.models.BreathingExerciseType
import com.oqza.myzenflow.data.models.UserStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ZenResponseGeneratorTest {

    /** Text is "[resourceId]" plus any format arguments, so tests can tell which string was used. */
    private val strings = ZenStrings { id, args -> "[$id]" + if (args.isEmpty()) "" else args.joinToString(",", "(", ")") }

    /** Dice that always roll [roll] (1..10) and always pick list index [pick]. */
    private class ScriptedRandom(private val roll: Int, private val pick: Int = 0) : Random() {
        override fun nextBits(bitCount: Int) = 0
        override fun nextInt(from: Int, until: Int) = roll
        override fun nextInt(until: Int) = pick.coerceAtMost(until - 1)
    }

    private val noNote = ScriptedRandom(roll = 1)
    private val alwaysNote = ScriptedRandom(roll = 10)

    private fun generator(random: Random = noNote, strings: ZenStrings = this.strings) = ZenResponseGenerator(strings, random)

    private fun id(@androidx.annotation.StringRes res: Int) = "[$res]"

    // ---------------------------------------------------------------- catalog

    @Test
    fun `every intent and sentiment has three templates`() {
        for (intent in ZenIntent.entries) for (sentiment in ZenSentiment.entries) {
            val ids = ZenResponseCatalog.templatesFor(intent, sentiment)
            assertEquals("$intent/$sentiment", 3, ids.size)
            assertEquals("$intent/$sentiment has duplicates", 3, ids.toSet().size)
        }
        assertEquals(20, ZenResponseCatalog.quotes.size)
    }

    @Test
    fun `reply is one of the templates for its intent and sentiment`() {
        val allowed = ZenResponseCatalog.templatesFor(ZenIntent.SLEEP, ZenSentiment.NEGATIVE).map { id(it) }
        val reply = generator().generate(ZenAnalysis(ZenIntent.SLEEP, ZenSentiment.NEGATIVE))
        assertTrue(reply.text in allowed)
        assertEquals(ZenIntent.SLEEP, reply.intent)
        assertEquals(ZenSentiment.NEGATIVE, reply.sentiment)
        assertFalse(reply.isCrisis)
    }

    @Test
    fun `different dice pick different variants`() {
        val analysis = ZenAnalysis(ZenIntent.FOCUS, ZenSentiment.NEUTRAL)
        val texts = (0..2).map { generator(ScriptedRandom(roll = 1, pick = it)).generate(analysis).text }
        assertEquals(3, texts.toSet().size)
    }

    // ---------------------------------------------------------------- crisis

    @Test
    fun `crisis gets the fixed reply with no button and no personal note`() {
        val stats = UserStats(currentStreak = 9, totalMinutes = 105)
        val reply = generator(alwaysNote).generate(ZenAnalysis(ZenIntent.STRESS, ZenSentiment.NEGATIVE, isCrisis = true), stats)
        assertEquals(id(R.string.zm_crisis_reply), reply.text)
        assertTrue(reply.isCrisis)
        assertNull(reply.action)
        assertEquals(ZenIntent.GENERAL, reply.intent)
    }

    // ---------------------------------------------------------------- personal note

    private val general = ZenAnalysis(ZenIntent.GENERAL, ZenSentiment.NEUTRAL)

    @Test
    fun `streak note is added on a lucky roll`() {
        val reply = generator(alwaysNote).generate(general, UserStats(currentStreak = 5))
        assertTrue(reply.text.endsWith("\n\n" + id(R.string.zm_personalization_streak) + "(5)"))
    }

    @Test
    fun `no note on an unlucky roll, without stats, or with a short streak`() {
        assertFalse(generator(noNote).generate(general, UserStats(currentStreak = 5)).text.contains("\n\n"))
        assertFalse(generator(alwaysNote).generate(general, null).text.contains("\n\n"))
        assertFalse(generator(alwaysNote).generate(general, UserStats(currentStreak = 2)).text.contains("\n\n"))
    }

    @Test
    fun `milestone note when minutes sit just past a hundred`() {
        val reply = generator(alwaysNote).generate(general, UserStats(totalMinutes = 105))
        assertTrue(reply.text.endsWith("\n\n" + id(R.string.zm_personalization_milestone) + "(105)"))
        assertFalse(generator(alwaysNote).generate(general, UserStats(totalMinutes = 150)).text.contains("\n\n"))
        assertFalse(generator(alwaysNote).generate(general, UserStats(totalMinutes = 50)).text.contains("\n\n"))
    }

    @Test
    fun `at most one note and the streak wins`() {
        val reply = generator(alwaysNote).generate(general, UserStats(currentStreak = 5, totalMinutes = 105))
        assertTrue(reply.text.contains(id(R.string.zm_personalization_streak)))
        assertFalse(reply.text.contains(id(R.string.zm_personalization_milestone)))
    }

    @Test
    fun `long replies get no note`() {
        val longStrings = ZenStrings { _, _ -> "x".repeat(300) }
        val reply = generator(alwaysNote, longStrings).generate(general, UserStats(currentStreak = 5))
        assertEquals(300, reply.text.length)
    }

    // ---------------------------------------------------------------- actions

    @Test
    fun `intents map to the right destination`() {
        assertEquals(ZenAction.Breathing(BreathingExerciseType.BOX_BREATHING.id, R.string.zm_action_breathing), ZenIntent.STRESS.action)
        assertEquals(ZenIntent.STRESS.action, ZenIntent.BREATHING.action)
        assertEquals(ZenAction.Breathing(BreathingExerciseType.TECHNIQUE_478.id, R.string.zm_action_478), ZenIntent.SLEEP.action)
        assertEquals(ZenAction.Breathing(BreathingExerciseType.CALMING_BREATH.id, R.string.zm_action_meditation), ZenIntent.MEDITATION.action)
        assertEquals(ZenAction.Focus(R.string.zm_action_focus), ZenIntent.FOCUS.action)
        assertEquals(ZenAction.Garden(R.string.zm_action_garden), ZenIntent.MOTIVATION.action)
        assertEquals(ZenAction.Garden(R.string.zm_action_progress), ZenIntent.PROGRESS.action)
        assertNull(ZenIntent.GENERAL.action)
    }

    @Test
    fun `reply action follows its intent`() {
        val reply = generator().generate(ZenAnalysis(ZenIntent.SLEEP, ZenSentiment.NEUTRAL))
        assertEquals(ZenIntent.SLEEP.action, reply.action)
    }
}
