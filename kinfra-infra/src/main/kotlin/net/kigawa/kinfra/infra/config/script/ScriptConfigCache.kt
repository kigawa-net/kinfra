package net.kigawa.kinfra.infra.config.script

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest

/**
 * .ktsスクリプトの評価結果（設定オブジェクト）を、ファイル内容のハッシュをキーに
 * ディスクキャッシュする。Kotlinスクリプトのコンパイルは1回あたり数百ms〜数秒かかるため、
 * 内容が変わっていないファイルについては毎回のCLI起動でこのコストを払わずに済むようにする。
 *
 * 制約: スクリプトの評価結果がファイル内容のみに依存し、環境変数など外部状態に
 * 依存しない（決定的である）ことを前提にしている。kinfraのDSLはその前提を満たす
 * 設計になっている（`bws()`はシークレットを即時解決せずマーカー文字列を返すのみ）。
 */
object ScriptConfigCache {
    private val cacheDir: File by lazy {
        File(System.getProperty("user.home"), ".local/kinfra/script-cache").also { it.mkdirs() }
    }
    private val json = Json { ignoreUnknownKeys = true }

    fun <T> loadOrEval(
        file: File,
        serializer: KSerializer<T>,
        eval: () -> T,
    ): T {
        val cacheFile = runCatching { File(cacheDir, "${sha256(file)}.json") }.getOrNull()

        if (cacheFile != null && cacheFile.exists()) {
            val cached = runCatching { json.decodeFromString(serializer, cacheFile.readText()) }
            if (cached.isSuccess) {
                return cached.getOrThrow()
            }
        }

        val value = eval()
        if (cacheFile != null) {
            runCatching { cacheFile.writeText(json.encodeToString(serializer, value)) }
        }
        return value
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
