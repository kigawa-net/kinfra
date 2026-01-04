package net.kigawa.kodel.api.entrypoint

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EntrypointTest {
    @Test
    fun `should create entrypoint with input and output types`() {
        // Given
        val entrypoint =
            object: Entrypoint<String, Int, Unit> {
                override val info = EntrypointInfo("test", emptyList(), "Test entrypoint")

                override fun access(input: String, ctx: Unit): Int = input.length
            }

        // When & Then
        assertNotNull(entrypoint.info)
        assertEquals("test", entrypoint.info.name.raw)
    }

    @Test
    fun `should access entrypoint with input`() = runTest {
        // Given
        val entrypoint =
            object: Entrypoint<String, Int, Unit> {
                override val info = EntrypointInfo("lengthcalc", emptyList(), "Calculates string length")

                override fun access(input: String, ctx: Unit): Int = input.length
            }

        // When
        val result = entrypoint.access("hello", Unit)

        // Then
        assertEquals(5, result)
    }

    @Test
    fun `should have entrypoint info`() {
        // Given
        val entrypoint =
            object: Entrypoint<Unit, Unit, Unit> {
                override val info = EntrypointInfo("simple", listOf("alias1"), "A simple entrypoint")

                override fun access(input: Unit, ctx: Unit) {}
            }

        // When
        val info = entrypoint.info

        // Then
        assertEquals("simple", info.name.raw)
        assertEquals("A simple entrypoint", info.description)
        assertEquals(1, info.aliases.size)
    }
}
