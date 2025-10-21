package world.onemc.domain.dep.context

interface ContextProvider<T: Any> {
    val value: T
    fun child(): ContextProvider<T>
    operator fun plus(contextProvider: ContextProvider<*>): ContextProvider<*>?
    suspend fun close()
}