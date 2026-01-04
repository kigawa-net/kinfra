package net.kigawa.kinfra.action.entrypoint

import net.kigawa.kinfra.model.input.KinfraInput
import net.kigawa.kodel.api.err.Res
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RootEntrypointsTest {
    @Test
    fun `should create RootEntrypoints`() {
        // Given & When
        val entrypoints = RootEntrypoints()

        // Then
        assertNotNull(entrypoints)
    }

    @Test
    fun `should have entrypoint info`() {
        // Given
        val entrypoints = RootEntrypoints()

        // When
        val info = entrypoints.info

        // Then
        assertNotNull(info)
        assertEquals("kinfra", info.name.raw)
        assertEquals("Kinfra command line tool", info.description)
    }

    @Test
    fun `should access with KinfraInput`() {
        // Given
        val entrypoints = RootEntrypoints()
        val input = KinfraInput()

        // When
        val result = entrypoints.access(input, Unit)

        // Then
        assertTrue(result is Res.Ok)
    }
}
