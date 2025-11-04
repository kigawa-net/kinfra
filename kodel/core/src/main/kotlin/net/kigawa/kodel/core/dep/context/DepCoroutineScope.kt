package net.kigawa.kodel.core.dep.context

import kotlinx.coroutines.*
import net.kigawa.kodel.api.dep.context.DepCoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> DepCoroutineScope.async(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T,
): Deferred<T> {
    return CoroutineScope(coroutineContext).async(context, start, block)
}

fun DepCoroutineScope(context: CoroutineContext = Dispatchers.Default) =
    NormalDepCoroutineScope(context)