package net.kigawa.kodel.api.entrypoint

/**
 * エントリーポイントを翻訳するクラス。
 * 入力と出力を変換して別のエントリーポイントに委譲する。
 *
 * @param I このエントリーポイントの入力型
 * @param O このエントリーポイントの出力型
 * @param J 委譲先エントリーポイントの入力型
 * @param P 委譲先エントリーポイントの出力型
 * @param T 委譲先エントリーポイントの型
 * @param entrypoint 委譲先エントリーポイント
 * @param translator トランスレーター関数
 */
class TranslateEntrypoint<in I, out O, in J, out P, T : Entrypoint<J, P>>(
    val entrypoint: T,
    private val translator: ((J) -> P).(I) -> O,
) : Entrypoint<I, O> {
    override val info: EntrypointInfo
        get() = entrypoint.info

    /**
     * エントリーポイントにアクセスし、出力を翻訳する。
     *
     * @param input 入力
     * @return 翻訳された出力
     */
    override fun access(input: I): O {
        return object : (J) -> P {
            override fun invoke(p1: J): P {
                return entrypoint.access(p1)
            }
        }.translator(input)
    }
}
