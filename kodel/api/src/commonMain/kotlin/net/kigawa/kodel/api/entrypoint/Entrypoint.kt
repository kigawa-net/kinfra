package net.kigawa.kodel.api.entrypoint

/**
 * エントリーポイントのインターフェース。
 * 入力を受け取り、出力を返す。
 *
 * @param I 入力の型
 * @param O 出力の型
 */
interface Entrypoint<in I, out O, C>: EntrypointNode<I, O, C> {

    /** エントリーポイントの情報 */
    val info: EntrypointInfo
    override fun flat(): List<FlattedEntrypoint<I, O, C>> {
        return listOf(FlattedEntrypoint(listOf(info), this))
    }
}
