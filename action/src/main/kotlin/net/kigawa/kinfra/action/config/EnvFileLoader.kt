package net.kigawa.kinfra.action.config

/**
 * .env ファイルを読み込むインターフェース
 */
interface EnvFileLoader {
    /**
     * .env ファイルから環境変数を読み込む
     * @param envFilePath .env ファイルのパス（デフォルト: カレントディレクトリの .env）
     * @return 環境変数のマップ
     */
    fun load(envFilePath: String = ".env"): Map<String, String>

    /**
     * .env ファイルから特定の環境変数を取得
     * 環境変数が設定されている場合はそちらを優先、なければ .env ファイルから取得
     * @param key 環境変数名
     * @param envFilePath .env ファイルのパス（デフォルト: カレントディレクトリの .env）
     * @return 環境変数の値、存在しない場合は null
     */
    fun get(key: String, envFilePath: String = ".env"): String?
}
