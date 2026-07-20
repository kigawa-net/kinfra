package net.kigawa.kodel.api.entrypoint

/**
 * エントリーポイントグループのベースクラス。
 * サブエントリーポイントを管理する。
 *
 * @param I 入力の型
 * @param O 出力の型
 */
abstract class EntrypointGroupBase<I, O> : Entrypoint<I, O> {
    private var subEntrypoints = mutableListOf<Entrypoint<I, O>>()
    /** サブエントリーポイントのリスト */
    val entrypoints: List<Entrypoint<I, O>>
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
    fun <J, P, T : Entrypoint<J, P>> add(
        endpoint: T,
        translator: ((J) -> P).(I) -> O,
    ): T {
        return endpoint.also { subEntrypoints += TranslateEntrypoint(endpoint, translator) }
    }
}
