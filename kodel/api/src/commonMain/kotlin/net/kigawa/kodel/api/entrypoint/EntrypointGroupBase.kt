package net.kigawa.kodel.api.entrypoint

/**
 * エントリーポイントグループのベースクラス。
 * サブエントリーポイントを管理する。
 *
 * @param I 入力の型
 * @param O 出力の型
 */
abstract class EntrypointGroupBase<I, O, C>: UnnamedEntrypointGroupBase<I, O, C>(), Entrypoint<I, O, C> {
    override fun access(
        input: I, ctx: C,
    ): O? {
        return super.access(input, ctx) ?: onSubEntrypointNotFound(input)
    }

    abstract fun onSubEntrypointNotFound(input: I): O?
    override fun flat(): List<FlattedEntrypoint<I, O, C>> {
        return entrypoints.flatMap { it.flat() }
            .map { FlattedEntrypoint(listOf(info) + it.path, it.entrypoint) } +
            FlattedEntrypoint(listOf(info), this)
    }
}
