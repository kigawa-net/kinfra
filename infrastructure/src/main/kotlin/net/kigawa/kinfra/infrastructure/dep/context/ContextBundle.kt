package world.onemc.domain.dep.context

import kotlin.reflect.KClass

class ContextBundle(
    var providers: List<ContextProvider<*>> = listOf(),
) {
    fun add(context: ContextProvider<*>): ContextBundle {
        providers += context
        return this
    }

    inline fun <reified T: Any> get(clazz: KClass<T> = T::class): T {
        return getOrNull(clazz) ?: throw NoSuchElementException(
            "context not found: ${clazz.qualifiedName}"
        )
    }
    inline fun <reified T: Any> getOrNull(clazz: KClass<T> = T::class): T? {
        return providers.map { it.value }.filterIsInstance(clazz.java).singleOrNull()
    }

    fun child(): ContextBundle {
        return ContextBundle(providers.map { it.child() })
    }

    operator fun plus(contextBundle: ContextBundle): ContextBundle {
        val src = contextBundle.providers.toMutableList()

        return ContextBundle(providers.map { provider ->
            var result = provider
            src.firstOrNull {
                result = provider.plus(it) ?: return@firstOrNull false
                true
            }?.let { src.remove(it) }
            result
        } + src)
    }

    suspend fun close() {
        val suppressed = mutableListOf<Exception>()
        providers.forEach {
            try {
                it.close()
            } catch (e: Exception) {
                suppressed.add(e)
            }
        }
        if (suppressed.isNotEmpty()) throw Exception("context close error")
            .apply { suppressed.forEach(::addSuppressed) }
    }
}
