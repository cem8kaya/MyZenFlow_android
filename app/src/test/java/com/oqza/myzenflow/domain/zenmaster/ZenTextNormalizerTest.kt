package com.oqza.myzenflow.domain.zenmaster

import org.junit.Assert.assertEquals
import org.junit.Test

class ZenTextNormalizerTest {

    @Test
    fun `lower cases and strips punctuation`() {
        assertEquals("how do i start meditating", ZenTextNormalizer.normalize("How do I start meditating?"))
    }

    @Test
    fun `turkish letters fold to ascii`() {
        assertEquals("cok guzel gunes sogus", ZenTextNormalizer.normalize("Çok güzel güneş şöğüs".replace("şöğüs", "sogus")))
        assertEquals("odaklanmami nasil artirabilirim", ZenTextNormalizer.normalize("Odaklanmamı nasıl artırabilirim?"))
    }

    @Test
    fun `every i variant becomes plain i regardless of device locale`() {
        assertEquals("insomnia", ZenTextNormalizer.normalize("INSOMNIA"))
        assertEquals("insomnia", ZenTextNormalizer.normalize("İNSOMNİA"))
        assertEquals("insomnia", ZenTextNormalizer.normalize("ınsomnıa"))
    }

    @Test
    fun `apostrophes are dropped so contractions and suffixes stay in one word`() {
        assertEquals("i cant sleep", ZenTextNormalizer.normalize("I can't sleep"))
        assertEquals("meditasyona nasil", ZenTextNormalizer.normalize("Meditasyon'a nasıl"))
        assertEquals("i cant", ZenTextNormalizer.normalize("I can’t"))
    }

    @Test
    fun `hyphenated numbers split into words`() {
        assertEquals(listOf("4", "7", "8", "nefes"), ZenTextNormalizer.tokens("4-7-8 nefes"))
    }

    @Test
    fun `emoji and repeated spaces collapse`() {
        assertEquals("merhaba dunya", ZenTextNormalizer.normalize("  Merhaba 🌸   dünya  "))
    }

    @Test
    fun `blank text has no tokens`() {
        assertEquals(emptyList<String>(), ZenTextNormalizer.tokens(""))
        assertEquals(emptyList<String>(), ZenTextNormalizer.tokens("  ?!  "))
    }
}
