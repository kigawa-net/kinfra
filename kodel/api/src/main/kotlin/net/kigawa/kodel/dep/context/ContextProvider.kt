package net.kigawa.kodel.dep.context

interface ContextProvider<T: Any> {
    val value: T
    fun child(): ContextProvider<T>
    operator fun plus(contextProvider: ContextProvider<*>): ContextProvider<*>?
    suspend fun close()
}