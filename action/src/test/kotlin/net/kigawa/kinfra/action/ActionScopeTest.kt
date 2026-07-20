package net.kigawa.kinfra.action

import kotlinx.coroutines.SupervisorJob
import net.kigawa.kodel.api.dep.DepProviderFactory
import net.kigawa.kodel.core.dep.DefaultDepProviders
import net.kigawa.kodel.core.dep.context.NormalDepCoroutineScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ActionScopeTest {
    @Test
    fun `should create ActionScope with dependencies`() {
        // Given
        class TestActionScope : ActionScope<TestActionScope> {
            override val defaultDepProviderFactory: DepProviderFactory = DefaultDepProviders.Singleton
            override val depCoroutineScope = NormalDepCoroutineScope(SupervisorJob())

            override fun plus(depScope: TestActionScope): TestActionScope = this

            override fun newDepScope(): TestActionScope = this

            override fun close() {
                depCoroutineScope.close()
            }

            override fun toString(): String = "TestActionScope(defaultDepProviderFactory=$defaultDepProviderFactory)"
        }

        // When
        val scope = TestActionScope()

        // Then
        assertNotNull(scope)
        assertEquals(DefaultDepProviders.Singleton, scope.defaultDepProviderFactory)
    }
}
