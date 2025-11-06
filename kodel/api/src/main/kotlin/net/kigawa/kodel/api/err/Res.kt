package net.kigawa.kodel.api.err

/**
 * 結果を表すシールドインターフェース。
 * 成功（Ok）またはエラー（Err）を表す。
 *
 * @param T 成功時の値の型
 * @param E エラーの型
 */
sealed interface Res<T, in E : Throwable> {
    /**
     * 成功を表すクラス。
     *
     * @param value 成功時の値
     */
    class Ok<T, E : Throwable>(val value: T) : Res<T, E>

    /**
     * エラーを表すクラス。
     *
     * @param err エラー
     */
    class Err<T, E : Throwable>(val err: E) : Res<T, E>
}
