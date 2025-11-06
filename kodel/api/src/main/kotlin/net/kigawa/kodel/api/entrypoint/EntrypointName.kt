package net.kigawa.kodel.api.entrypoint

/**
 * エントリーポイントの名前を表すデータクラス。
 * 小文字、数字、ハイフンのみ許可。
 *
 * @param raw 生の名前文字列
 * @throws IllegalArgumentException 名前が空白、スペースを含む、または不正な文字を含む場合
 */
data class EntrypointName(
    val raw: String,
) {
    init {
        require(raw.isNotBlank()) { "entrypoint name cannot be blank" }
        require(!raw.contains(" ")) { "entrypoint name cannot contain space" }
        raw.forEach {
            if (it == '-') return@forEach
            if (it.isDigit()) return@forEach
            require(it.isLowerCase()) { "entrypoint name must be lowercase" }
            require(it.isLetterOrDigit()) {
                "entrypoint name can contain only lowercase letters, digits, hyphen"
            }
        }
    }
}
