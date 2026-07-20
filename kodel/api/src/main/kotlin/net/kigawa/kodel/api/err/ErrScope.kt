package net.kigawa.kodel.api.err

/**
 * エラー処理スコープのクラス。
 * エラーを投げるためのメソッドを提供する。
 *
 * @param E エラーの型
 */
class ErrScope<in E : Throwable> {
    /**
     * エラーを投げる。
     *
     * @param err 投げるエラー
     */
    fun err(err: E): Nothing = throw err
}

/**
 * エラー処理を試行する関数。
 * 成功時はOk、指定されたエラー時はErrを返す。
 *
 * @param T 結果の型
 * @param E エラーの型
 * @param block 実行するブロック
 * @return 結果またはエラー
 */
inline fun <T, reified E : Throwable> tryErr(
    block: context(ErrScope<E>)
    () -> T,
): Res<T, E> {
    return ErrScope<E>().let {
        try {
            Res.Ok(block(it))
        } catch (e: Throwable) {
            if (e !is E) throw e
            Res.Err(e)
        }
    }
}
