package net.kigawa.kinfra.infrastructure.dep

import kotlinx.coroutines.SupervisorJob
import net.kigawa.kodel.core.dep.DefaultDepProviders
import net.kigawa.kodel.core.dep.context.NormalDepCoroutineScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class KinfraDepScopeTest {
    @Test
    fun `should create KinfraDepScope with dependencies`() {
        // Given
        val coroutineScope = NormalDepCoroutineScope(SupervisorJob())

        // When
        val scope = KinfraDepScope(coroutineScope)

        // Then
        assertNotNull(scope)
        assertEquals(DefaultDepProviders.Lazy, scope.depProviderFactory)
    }
}
