package net.kigawa.kodel.api.entrypoint

/**
 * エントリーポイントの情報を表すクラス。
 *
 * @param name エントリーポイントの名前
 * @param aliases エントリーポイントのエイリアス
 * @param description エントリーポイントの説明
 */
class EntrypointInfo(
    val name: EntrypointName,
    val aliases: List<EntrypointName>,
    val description: String,
) {
    /**
     * 文字列からEntrypointInfoを作成するコンストラクタ。
     *
     * @param name エントリーポイントの名前（文字列）
     * @param aliases エントリーポイントのエイリアス（文字列リスト）
     * @param description エントリーポイントの説明
     */
    constructor(name: String, aliases: List<String>, description: String) :
        this(EntrypointName(name), aliases.map(::EntrypointName), description)
}
