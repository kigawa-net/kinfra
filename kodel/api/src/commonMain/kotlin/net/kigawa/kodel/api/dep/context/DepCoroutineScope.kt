package net.kigawa.kodel.api.dep.context

import kotlin.coroutines.CoroutineContext

/**
 * 依存コルーチンスコープのインターフェース。
 * コルーチンの起動と管理を提供する。
 */
interface DepCoroutineScope {
    /** コルーチンコンテキスト */
    val coroutineContext: CoroutineContext

    /**
     * コルーチンを起動する。
     *
     * @param onFail 失敗時のコールバック
     * @param onCancel キャンセル時のコールバック
     * @param onNonComplete 非完了時のコールバック
     * @param onFinally 最終コールバック
     * @param block 実行するブロック
     */
    fun launch(
        onFail: (e: Exception) -> Unit = {
            println("Error in ${DepCoroutineScope::launch}: ${it.message}")
            it.printStackTrace()
        },
        onCancel: () -> Unit = {},
        onNonComplete: () -> Unit = {},
        onFinally: () -> Unit = {},
        block: suspend DepCoroutineScope.() -> Unit,
    )
    override fun toString(): String
}
