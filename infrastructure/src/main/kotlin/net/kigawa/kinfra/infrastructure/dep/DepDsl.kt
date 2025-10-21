package world.onemc.domain.dep

import net.kigawa.kinfra.infrastructure.coroutine.asScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import net.kigawa.kinfra.infrastructure.dep.Dep
import net.kigawa.kinfra.infrastructure.dep.DepContext
import net.kigawa.kinfra.infrastructure.dep.context.ContextBundle
import kotlin.coroutines.CoroutineContext

class DepDsl(
    override var contextBundle: ContextBundle,
): DepContext {
    private val closeHooks = MutableStateFlow<List<suspend () -> Unit>?>(listOf())
    private val built = MutableStateFlow<List<Dep<*>>?>(listOf())

    companion object {
        fun <T> exec(
            contextBundle: ContextBundle,
            block: suspend DepDsl.() -> T,
        ): Dep<T> = DepDsl(contextBundle.child()).let {
            it.build(contextBundle.get<CoroutineContext>().asScope().async {
                supervisorScope {
                    try {
                        it.block()
                    } catch (e: Exception) {
                        throw RuntimeException("scope error", e)
                    }
                }
            })
        }

    }

    override fun closeHook(block: suspend () -> Unit) {
        closeHooks.updateAndGet {
            if (it == null) null
            else it + block
        } ?: contextBundle.get<CoroutineContext>().asScope().launch {
            block()
        }
    }

    fun <T> build(result: Deferred<T>): Dep<T> {
        return Dep(closeHooks, result, contextBundle).also { scope ->
            contextBundle.get<CoroutineContext>().asScope().launch {
                supervisorScope {
                    built.updateAndGet {
                        if (it == null) null
                        else it + scope
                    } ?: scope.close()
                }
            }
        }
    }

    suspend fun <U> Dep<U>.get(): U {
        this@DepDsl.contextBundle = this@get.contextBundle +
            this@DepDsl.contextBundle
        this@get.closeHooks.updateAndGet {
            if (it == null) null
            else it + { closeScopes() }
        } ?: closeScopes()
        return this@get.result.await()
    }

    private suspend fun closeScopes() {
        val suppressed = mutableListOf<Exception>()
        built.getAndUpdate { null }?.forEach {
            try {
                it.close()
            } catch (e: Exception) {
                suppressed.add(e)
            }
        }
        if (suppressed.isNotEmpty()) throw Exception("scope close error")
            .apply { suppressed.forEach(::addSuppressed) }
    }
}
