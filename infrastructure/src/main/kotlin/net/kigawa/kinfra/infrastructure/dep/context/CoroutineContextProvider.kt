package net.kigawa.kinfra.infrastructure.dep.context

import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import world.onemc.domain.dep.context.ContextProvider
import kotlin.coroutines.CoroutineContext

class CoroutineContextProvider(
    val coroutineContext: CoroutineContext,
): ContextProvider<CoroutineContext> {
    override val value: CoroutineContext
        get() = coroutineContext + SupervisorJob()

    override fun child(): ContextProvider<CoroutineContext> {
        return CoroutineContextProvider(value)
    }

    override fun plus(
        contextProvider: ContextProvider<*>,
    ): ContextProvider<*>? = contextProvider.value
        .let { it as? CoroutineContext }
        ?.let { CoroutineContextProvider(value + it) }


    override suspend fun close() {
        coroutineContext.cancel()
    }
}