package net.kigawa.kodel.core.dep.context

import kotlinx.coroutines.*
import net.kigawa.kodel.api.dep.context.DepCoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * 通常の依存コルーチンスコープの実装。
 * コルーチンの起動と管理を行う。
 *
 * @param ownCoroutineContext 独自のコルーチンコンテキスト
 */
class NormalDepCoroutineScope(
    private val ownCoroutineContext: CoroutineContext,
): DepCoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = ownCoroutineContext + SupervisorJob()

    companion object {
        /**
         * NormalDepCoroutineScopeを作成する。
         *
         * @param context コルーチンコンテキスト
         * @return NormalDepCoroutineScopeのインスタンス
         */
        fun create(context: CoroutineContext = Dispatchers.Default) = NormalDepCoroutineScope(context)
    }

    override fun launch(
        onFail: (e: Exception) -> Unit,
        onCancel: () -> Unit,
        onNonComplete: () -> Unit,
        onFinally: () -> Unit,
        block: suspend DepCoroutineScope.() -> Unit,
    ) {
        val job =
            CoroutineScope(coroutineContext).launch {
                try {
                    block(this@NormalDepCoroutineScope)
                } catch (e: Exception) {
                    if (e is CancellationException) return@launch
                    onFail(e)
                }
            }
        CoroutineScope(coroutineContext).launch(start = CoroutineStart.ATOMIC) {
            job.join()
            if (job.isCancelled) onCancel()
            if (!job.isCompleted) onNonComplete()
            onFinally()
        }
    }

    override fun toString(): String {
        return "NormalDepCoroutineScope(ownCoroutineContext=$ownCoroutineContext)"
    }

    /**
     * 別のスコープと結合する。
     *
     * @param depCoroutineScope 結合するスコープ
     * @return 結合されたスコープ
     */
    fun plus(depCoroutineScope: NormalDepCoroutineScope): NormalDepCoroutineScope {
        return NormalDepCoroutineScope(ownCoroutineContext + depCoroutineScope.ownCoroutineContext + SupervisorJob())
    }

    /**
     * 新しいスコープを作成する。
     *
     * @return 新しいNormalDepCoroutineScope
     */
    fun newScope(): NormalDepCoroutineScope {
        return NormalDepCoroutineScope(coroutineContext)
    }

    /**
     * スコープをクローズする。
     */
    fun close() {
        ownCoroutineContext.cancel()
    }
}
