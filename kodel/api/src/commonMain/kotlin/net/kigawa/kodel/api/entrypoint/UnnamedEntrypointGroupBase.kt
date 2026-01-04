package net.kigawa.kodel.api.entrypoint

/**
 * エントリーポイントグループのベースクラス。
 * サブエントリーポイントを管理する。
 *
 * @param I 入力の型
 * @param O 出力の型
 */
abstract class UnnamedEntrypointGroupBase<I, O, C>: EntrypointNode<I, O, C> {
    private var subEntrypoints = mutableListOf<EntrypointNode<I, O, C>>()

    /** サブエントリーポイントのリスト */
    val entrypoints: List<EntrypointNode<I, O, C>>
        get() = subEntrypoints

    /**
     * エントリーポイントを追加する。
     *
     * @param J 追加するエントリーポイントの入力型
     * @param P 追加するエントリーポイントの出力型
     * @param T エントリーポイントの型
     * @param endpoint 追加するエントリーポイント
     * @param translator トランスレーター関数
     * @return 追加されたエントリーポイント
     */
    fun <J, P, T: EntrypointNode<J, P, C>> add(
        endpoint: T,
        translator: ((J?) -> P?).(I) -> O?,
    ): T {
        return endpoint.also { subEntrypoints += TranslateEntrypoint(endpoint, translator) }
    }

    override fun access(
        input: I, ctx: C,
    ): O? {
        entrypoints.forEach { entrypoint ->
            entrypoint.access(input, ctx)?.let { return it }
        }
        return null
    }

    override fun flat(): List<FlattedEntrypoint<I, O, C>> {
        return subEntrypoints.flatMap { it.flat() }
    }
}
