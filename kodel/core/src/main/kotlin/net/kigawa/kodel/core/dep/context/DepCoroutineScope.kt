package net.kigawa.kodel.core.dep.context

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.kigawa.kodel.api.dep.context.DepCoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * DepCoroutineScopeの拡張関数。
 * 非同期タスクを開始する。
 *
 * @param T 結果の型
 * @param context コルーチンコンテキスト
 * @param start 開始モード
 * @param block 実行するブロック
 * @return Deferredオブジェクト
 */
fun <T> DepCoroutineScope.async(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T,
): Deferred<T> {
    return CoroutineScope(coroutineContext).async(context, start, block)
}


