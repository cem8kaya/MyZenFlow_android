package com.oqza.myzenflow.localization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Guards the Zen Master chat content (zen_master_strings.xml). The first release ships en + tr;
 * other locales fall back to English on purpose, so this checks only those two. Every
 * intent x sentiment pair must keep its three reply variants, because the response generator
 * picks one at random.
 */
class ZenMasterStringsTest {

    private val resDir = listOf(File("src/main/res"), File("app/src/main/res")).first { it.exists() }

    private val intents = listOf("stress", "focus", "sleep", "breathing", "motivation", "meditation", "progress", "general")
    private val sentiments = listOf("positive", "neutral", "negative")

    private fun load(dir: String): Map<String, String> {
        val file = File(resDir, "$dir/zen_master_strings.xml")
        assertTrue("Missing ${file.path}", file.exists())
        val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            .documentElement.getElementsByTagName("string")
        return (0 until nodes.length).associate { nodes.item(it).attributes.getNamedItem("name").nodeValue to nodes.item(it).textContent }
    }

    private val en by lazy { load("values") }
    private val tr by lazy { load("values-tr") }

    private fun placeholders(text: String) =
        Regex("%(\\d+\\$)?[sd]|%%").findAll(text).map { it.value }.sorted().toList()

    @Test
    fun `English and Turkish define the same strings`() {
        assertEquals("Only in en: ${en.keys - tr.keys}", emptySet<String>(), en.keys - tr.keys)
        assertEquals("Only in tr: ${tr.keys - en.keys}", emptySet<String>(), tr.keys - en.keys)
    }

    @Test
    fun `format placeholders match between languages`() {
        en.forEach { (key, value) ->
            assertEquals("Placeholders differ for $key", placeholders(value), placeholders(tr.getValue(key)))
        }
    }

    @Test
    fun `every intent and sentiment has three reply variants`() {
        for (intent in intents) for (sentiment in sentiments) for (variant in 0..2) {
            val key = "zm_response_${intent}_${sentiment}_$variant"
            assertTrue("Missing $key", key in en)
        }
    }

    @Test
    fun `fallback reply and twenty quotes exist`() {
        assertTrue("zm_response_fallback" in en)
        for (i in 0..19) assertTrue("Missing zm_quote_$i", "zm_quote_$i" in en)
    }

    @Test
    fun `no string is blank or has an unresolved template marker`() {
        (en + tr.mapKeys { "tr:${it.key}" }).forEach { (key, value) ->
            assertTrue("Blank string $key", value.isNotBlank())
            assertTrue("Unresolved marker in $key", !value.contains("{q") && !value.contains("{{"))
        }
    }

    @Test
    fun `streak and milestone replies take exactly one number`() {
        listOf("zm_personalization_streak", "zm_personalization_milestone").forEach { key ->
            assertEquals(listOf("%1\$d"), placeholders(en.getValue(key)))
        }
    }
}
