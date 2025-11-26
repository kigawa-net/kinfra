package net.kigawa.kodel.api.dep

import net.kigawa.kodel.api.dep.context.DepScope
import kotlin.reflect.KClass

/**
 * 依存コンテキストを表すクラス。
 * 依存スコープとクローズフックを管理する。
 *
 * @param S 依存スコープの型
 * @param depScope 依存スコープ
 */
class DepContext<S: DepScope<S>>(
    var depScope: S,
    val depClass: KClass<*>,
) {
    val parentContexts = mutableListOf<DepContext<S>>()
    var closeHooks = listOf<suspend () -> Unit>({ depScope.close() })

    /**
     * 親依存スコープを追加する。
     *
     * @param depScope 追加する依存スコープ
     */
    fun appendParentDepContext(depContext: DepContext<S>) {
        depContext.checkCircularReference(this)
        parentContexts += depContext
        this.depScope += depContext.depScope
        closeHook { depScope.close() }
    }

    @Throws(CircularDepException::class)
    fun checkCircularReference(depContext: DepContext<S>) {
        if (this == depContext) throw CircularDepException(listOf(depClass))
        try {
            parentContexts.forEach { it.checkCircularReference(depContext) }
        } catch (e: CircularDepException) {
            throw CircularDepException(e.depClasses + depClass)
        }
    }

    /**
     * 新しい依存コンテキストを作成する。
     *
     * @return 新しい依存コンテキスト
     */
    fun newDepContext(depClass: KClass<*>): DepContext<S> {
        return DepContext(depScope.newDepScope(), depClass).also { closeHook { it.close() } }
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

    override fun toString(): String {
        return "DepContext(depScope=$depScope, closeHooks=${closeHooks.size})"
    }
}
