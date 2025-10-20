package world.onemc.domain.dep

import kotlinx.coroutines.CoroutineScope
import world.onemc.domain.dep.context.ContextBundle
import world.onemc.domain.dep.context.CoroutineContextProvider

fun <T> rootDep(coroutineScope: CoroutineScope, block: DepDsl.() -> T) =
    DepDsl.exec(
        ContextBundle(
            listOf(CoroutineContextProvider(coroutineScope.coroutineContext))
        ),
        block = block
    )
