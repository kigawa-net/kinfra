package net.kigawa.kinfra.infrastructure.dep

import net.kigawa.kinfra.infrastructure.dep.context.ContextBundle
import world.onemc.domain.dep.DepDsl
import kotlin.reflect.KClass

interface DepContext {
    val contextBundle: ContextBundle
    fun <T> dep(block: suspend DepDsl.() -> T): Dep<T> {
        return DepDsl.Companion.exec(contextBundle, block = block)
    }

    fun closeHook(block: suspend () -> Unit)
}

inline fun <reified T: Any> DepContext.getContext(clazz: KClass<T> = T::class): T = contextBundle.get(clazz)