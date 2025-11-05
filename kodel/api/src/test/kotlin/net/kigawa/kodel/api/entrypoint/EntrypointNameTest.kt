package net.kigawa.kodel.api.entrypoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EntrypointNameTest {
    @Test
    fun `should create entrypoint name from string`() {
        // Given
        val name = "test-entrypoint"

        // When
        val entrypointName = EntrypointName(name)

        // Then
        assertNotNull(entrypointName)
        assertEquals(name, entrypointName.raw)
    }

    @Test
    fun `should handle different name formats`() {
        // Given
        val simpleNotation = EntrypointName("simple")
        val hyphenNotation = EntrypointName("test-entrypoint")
        val numberNotation = EntrypointName("test123")

        // When & Then
        assertEquals("simple", simpleNotation.raw)
        assertEquals("test-entrypoint", hyphenNotation.raw)
        assertEquals("test123", numberNotation.raw)
    }

    @Test
    fun `should compare entrypoint names`() {
        // Given
        val name1 = EntrypointName("test")
        val name2 = EntrypointName("test")
        val name3 = EntrypointName("other")

        // When & Then
        assertEquals(name1.raw, name2.raw)
        assertEquals(false, name1.raw == name3.raw)
    }
}
