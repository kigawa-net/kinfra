package net.kigawa.kinfra.infrastructure.dep

import kotlinx.coroutines.CoroutineScope
import net.kigawa.kinfra.infrastructure.dep.context.ContextBundle
import net.kigawa.kinfra.infrastructure.dep.context.CoroutineContextProvider
import world.onemc.domain.dep.DepDsl

fun <T> rootDep(coroutineScope: CoroutineScope, block: DepDsl.() -> T) =
    DepDsl.Companion.exec(
        ContextBundle(
            listOf(CoroutineContextProvider(coroutineScope.coroutineContext))
        ),
        block = block
    )
