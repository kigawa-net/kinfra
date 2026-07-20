package net.kigawa.kinfra.model.conf

/**
 * kinfra.kts/kinfra-parent.ktsのDSLで`bws("secret-key")`が返す遅延参照マーカー。
 * スクリプト評価時点ではBitwarden認証が無いコマンド（sub list等）でも設定を読めるよう、
 * 即座には解決せずこのマーカー文字列を値として保持する。実際にterraformを呼び出す
 * 直前（TerraformServiceImpl、CurrentPlanAction等）でBitwarden Secret Managerから解決する。
 */
object BwsMarker {
    private const val PREFIX = " __kinfra_bws__:"
    private const val SUFFIX = " "

    fun wrap(secretKey: String): String = "$PREFIX$secretKey$SUFFIX"

    fun extractKey(value: String): String? {
        if (!value.startsWith(PREFIX) || !value.endsWith(SUFFIX)) return null
        return value.removePrefix(PREFIX).removeSuffix(SUFFIX)
    }

    /**
     * valueがbws()マーカーであればresolverで解決した値を返す。マーカーでなければそのまま返す。
     * resolverがnullを返した場合（シークレットが見つからない場合）も元の値をそのまま返す。
     */
    fun resolve(
        value: String,
        resolver: (String) -> String?,
    ): String {
        val key = extractKey(value) ?: return value
        return resolver(key) ?: value
    }
}
