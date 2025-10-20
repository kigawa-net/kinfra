package world.onemc.domain.dep

import world.onemc.domain.dep.context.ContextBundle
import kotlin.reflect.KClass

interface DepContext {
    val contextBundle: ContextBundle
    fun <T> dep(block: suspend DepDsl.() -> T): Dep<T> {
        return DepDsl.exec(contextBundle, block = block)
    }

    fun closeHook(block: suspend () -> Unit)
}

inline fun <reified T: Any> DepContext.getContext(clazz: KClass<T> = T::class): T = contextBundle.get(clazz)