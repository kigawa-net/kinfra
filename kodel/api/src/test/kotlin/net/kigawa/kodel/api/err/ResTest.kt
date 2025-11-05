package net.kigawa.kodel.api.err

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ResTest {
    @Test
    fun `should create Ok result`() {
        // Given
        val value = 42

        fun createResult(): Res<Int, Exception> = Res.Ok(value)

        // When
        val result = createResult()

        // Then
        when (result) {
            is Res.Ok -> assertEquals(value, result.value)
            is Res.Err -> fail("Expected Ok but got Err")
        }
    }

    @Test
    fun `should create Err result`() {
        // Given
        val error = Exception("Test error")

        fun createResult(): Res<Int, Exception> = Res.Err(error)

        // When
        val result = createResult()

        // Then
        when (result) {
            is Res.Ok -> fail("Expected Err but got Ok")
            is Res.Err -> assertEquals(error, result.err)
        }
    }

    @Test
    fun `should handle string values`() {
        // Given
        fun createOk(): Res<String, Exception> = Res.Ok("success")

        fun createErr(): Res<String, Exception> = Res.Err(Exception("failure"))

        // When
        val okResult = createOk()
        val errResult = createErr()

        // Then
        when (okResult) {
            is Res.Ok -> assertEquals("success", okResult.value)
            is Res.Err -> fail("Expected Ok but got Err")
        }

        when (errResult) {
            is Res.Ok -> fail("Expected Err but got Ok")
            is Res.Err -> assertEquals("failure", errResult.err.message)
        }
    }

    @Test
    fun `should support when expression`() {
        // Given
        fun processResult(result: Res<Int, Exception>): Int =
            when (result) {
                is Res.Ok -> result.value
                is Res.Err -> -1
            }

        // When
        val okValue = processResult(Res.Ok(100))
        val errValue = processResult(Res.Err(Exception("error")))

        // Then
        assertEquals(100, okValue)
        assertEquals(-1, errValue)
    }

    @Test
    fun `should handle complex types`() {
        // Given
        data class User(val id: Int, val name: String)
        val user = User(1, "Alice")

        fun createResult(): Res<User, Exception> = Res.Ok(user)

        // When
        // Then
        when (val result = createResult()) {
            is Res.Ok -> {
                assertEquals(user, result.value)
                assertEquals(1, result.value.id)
                assertEquals("Alice", result.value.name)
            }
            is Res.Err -> fail("Expected Ok but got Err")
        }
    }

    @Test
    fun `should handle ActionException`() {
        // Given
        val exception = ActionException(1, "Command failed")

        fun createResult(): Res<Unit, ActionException> = Res.Err(exception)

        // When
        val result = createResult()

        // Then
        when (result) {
            is Res.Ok -> fail("Expected Err but got Ok")
            is Res.Err -> {
                assertEquals(1, result.err.exitCode)
                assertEquals("Command failed", result.err.message)
            }
        }
    }
}
