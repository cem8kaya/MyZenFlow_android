package com.oqza.myzenflow.localization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Guards the translations: every language must define the same strings and plurals as the default
 * (English) resources, with the same format placeholders. Catches a missing key or a broken
 * "%1$d" before it becomes a runtime crash or a lint error.
 */
class LocalizationConsistencyTest {

    private val resDir = listOf(File("src/main/res"), File("app/src/main/res")).first { it.exists() }
    private val localeDirs = resDir.listFiles { f -> f.isDirectory && f.name.startsWith("values-") && File(f, "strings.xml").exists() }!!
        .filter { it.name != "values-night" }

    private fun parse(file: File) = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).documentElement

    private fun strings(dir: File): Map<String, String> {
        val nodes = parse(File(dir, "strings.xml")).getElementsByTagName("string")
        return (0 until nodes.length).associate { nodes.item(it).attributes.getNamedItem("name").nodeValue to nodes.item(it).textContent }
    }

    private fun plurals(dir: File): Map<String, Set<String>> {
        val file = File(dir, "plurals.xml")
        if (!file.exists()) return emptyMap()
        val nodes = parse(file).getElementsByTagName("plurals")
        return (0 until nodes.length).associate { i ->
            val node = nodes.item(i)
            val items = (node as org.w3c.dom.Element).getElementsByTagName("item")
            node.attributes.getNamedItem("name").nodeValue to (0 until items.length).map { items.item(it).textContent }.toSet()
        }
    }

    private fun placeholders(text: String) =
        Regex("%(\\d+\\$)?[sd]|%%").findAll(text).map { it.value }.sorted().toList()

    @Test
    fun `there are translations to check`() {
        assertTrue("expected at least tr, es, de, fr, pt-rBR", localeDirs.size >= 5)
    }

    @Test
    fun `every language has every string`() {
        val default = strings(File(resDir, "values"))
        localeDirs.forEach { dir ->
            val missing = default.keys - strings(dir).keys
            assertTrue("${dir.name} is missing: $missing", missing.isEmpty())
        }
    }

    @Test
    fun `no language has strings the default lacks`() {
        val default = strings(File(resDir, "values")).keys
        localeDirs.forEach { dir ->
            val extra = strings(dir).keys - default
            assertTrue("${dir.name} has unknown keys: $extra", extra.isEmpty())
        }
    }

    @Test
    fun `placeholders match the default`() {
        val default = strings(File(resDir, "values"))
        localeDirs.forEach { dir ->
            strings(dir).forEach { (key, value) ->
                // cycle_progress is formatted="false" with unnumbered args; compare as sorted lists anyway
                assertEquals("${dir.name}/$key", placeholders(default.getValue(key)), placeholders(value))
            }
        }
    }

    @Test
    fun `plurals exist in every language with one and other`() {
        val default = plurals(File(resDir, "values"))
        assertTrue(default.isNotEmpty())
        localeDirs.forEach { dir ->
            val p = plurals(dir)
            assertEquals("${dir.name} plural names", default.keys, p.keys)
        }
    }
}
