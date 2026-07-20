package net.kigawa.kodel.api.err

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ActionExceptionTest {
    @Test
    fun `should create exception with exit code and message`() {
        // Given
        val exitCode = 1
        val message = "Command failed"

        // When
        val exception = ActionException(exitCode, message)

        // Then
        assertEquals(exitCode, exception.exitCode)
        assertEquals(message, exception.message)
    }

    @Test
    fun `should create exception with exit code only`() {
        // Given
        val exitCode = 2

        // When
        val exception = ActionException(exitCode)

        // Then
        assertEquals(exitCode, exception.exitCode)
        assertEquals(null, exception.message)
    }

    @Test
    fun `should be throwable`() {
        // Given
        val exception = ActionException(1, "Test error")

        // When
        var caughtException: ActionException?
        try {
            throw exception
        } catch (e: ActionException) {
            caughtException = e
        }

        // Then
        assertNotNull(caughtException)
        assertEquals(1, caughtException.exitCode)
        assertEquals("Test error", caughtException.message)
    }

    @Test
    fun `should handle different exit codes`() {
        // Given & When
        val exception0 = ActionException(0, "Success")
        val exception1 = ActionException(1, "General error")
        val exception127 = ActionException(127, "Command not found")
        val exception130 = ActionException(130, "Interrupted")

        // Then
        assertEquals(0, exception0.exitCode)
        assertEquals(1, exception1.exitCode)
        assertEquals(127, exception127.exitCode)
        assertEquals(130, exception130.exitCode)
    }
}
