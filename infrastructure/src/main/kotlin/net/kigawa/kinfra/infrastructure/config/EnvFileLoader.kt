package net.kigawa.kinfra.infrastructure.config

import net.kigawa.kinfra.model.config.EnvFileLoader
import java.io.File

/**
 * .env ファイルを読み込む実装
 */
class EnvFileLoaderImpl : EnvFileLoader {

    /**
     * .env ファイルから環境変数を読み込む
     * @param envFilePath .env ファイルのパス（デフォルト: カレントディレクトリの .env）
     * @return 環境変数のマップ
     */
    override fun load(envFilePath: String): Map<String, String> {
        val envFile = File(envFilePath)
        if (!envFile.exists() || !envFile.canRead()) {
            return emptyMap()
        }

        val envVars = mutableMapOf<String, String>()

        envFile.readLines().forEach { line ->
            val trimmedLine = line.trim()

            // 空行やコメント行をスキップ
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                return@forEach
            }

            // KEY=VALUE 形式をパース
            val parts = trimmedLine.split("=", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim()
                envVars[key] = value
            }
        }

        return envVars
    }

    /**
     * .env ファイルから特定の環境変数を取得
     * 環境変数が設定されている場合はそちらを優先、なければ .env ファイルから取得
     * @param key 環境変数名
     * @param envFilePath .env ファイルのパス（デフォルト: カレントディレクトリの .env）
     * @return 環境変数の値、存在しない場合は null
     */
    override fun get(key: String, envFilePath: String): String? {
        // 環境変数を優先
        System.getenv(key)?.let { return it }

        // .env ファイルから取得
        return load(envFilePath)[key]
    }
}
