package net.kigawa.kodel.api.dep

import net.kigawa.kodel.api.dep.context.DepScope

/**
 * 依存コンテキストを表すクラス。
 * 依存スコープとクローズフックを管理する。
 *
 * @param S 依存スコープの型
 * @param depScope 依存スコープ
 */
class DepContext<S: DepScope<S>>(
    var depScope: S,
) {
    var closeHooks = listOf<suspend () -> Unit>({ depScope.close() })

    /**
     * 親依存スコープを追加する。
     *
     * @param depScope 追加する依存スコープ
     */
    fun appendParentDepScope(depScope: S) {
        this.depScope += depScope
        closeHook { depScope.close() }
    }

    /**
     * 新しい依存コンテキストを作成する。
     *
     * @return 新しい依存コンテキスト
     */
    fun newDepContext(): DepContext<S> {
        return DepContext(depScope.newDepScope()).also { closeHook { it.close() } }
    }

    /**
     * クローズフックを追加する。
     *
     * @param block クローズ時に実行するブロック
     */
    fun closeHook(block: suspend () -> Unit) {
        closeHooks += block
    }

    /**
     * コンテキストをクローズする。
     * 登録されたクローズフックを逆順に実行する。
     */
    suspend fun close() {
        closeHooks.reversed().forEach {
            try {
                it()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}
