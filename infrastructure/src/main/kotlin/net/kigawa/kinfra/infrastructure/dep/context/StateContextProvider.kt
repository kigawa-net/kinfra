package world.onemc.domain.dep.context

import net.kigawa.hakate.api.state.StateContext

class StateContextProvider(
    val stateContext: StateContext,
): ContextProvider<StateContext> {
    override val value: StateContext
        get() = stateContext.newStateContext()

    override fun child(): ContextProvider<StateContext> {
        return StateContextProvider(value)
    }

    override fun plus(
        contextProvider: ContextProvider<*>,
    ): ContextProvider<*>? = contextProvider.value
        .let { it as? StateContext }
        ?.let { StateContextProvider(it.merge(value)) }

    override suspend fun close() {
        stateContext.cancel()
    }
}