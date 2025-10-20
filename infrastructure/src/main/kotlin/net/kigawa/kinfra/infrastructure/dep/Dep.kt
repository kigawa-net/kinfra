package world.onemc.domain.dep

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import world.onemc.domain.dep.context.ContextBundle

class Dep<T>(
    internal val closeHooks: MutableStateFlow<List<suspend () -> Unit>?>,
    internal val result: Deferred<T>,
    val contextBundle: ContextBundle,
) {
    suspend fun close() {
        val suppressed = mutableListOf<Exception>()
        closeHooks.getAndUpdate { null }?.forEach {
            try {
                it()
            } catch (e: Exception) {
                suppressed.add(e)
            }
        }
        try {
            contextBundle.close()
        } catch (e: Exception) {
            suppressed.add(e)
        }
        if (suppressed.isNotEmpty()) throw Exception("scope close error")
            .apply { suppressed.forEach(::addSuppressed) }
    }

    fun <U> sub(block: DepDsl.(T) -> U): Dep<U> {
        return DepDsl.exec(contextBundle.child()) {
            println("sub scope")
            block(result.await())
        }
    }
}
