package net.kigawa.kodel.api.entrypoint

/**
 * エントリーポイントのインターフェース。
 * 入力を受け取り、出力を返す。
 *
 * @param I 入力の型
 * @param O 出力の型
 */
interface Entrypoint<in I, out O> {
    /** エントリーポイントの情報 */
    val info: EntrypointInfo

    /**
     * エントリーポイントにアクセスする。
     *
     * @param input 入力
     * @return 出力
     */
    fun access(input: I): O
}
