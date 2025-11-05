package net.kigawa.kodel.core.dep.context

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

fun depCoroutineScope(context: CoroutineContext = Dispatchers.Default) = NormalDepCoroutineScope(context)
