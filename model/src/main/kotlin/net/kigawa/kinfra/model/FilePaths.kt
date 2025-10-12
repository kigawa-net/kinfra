package net.kigawa.kinfra.model

/**
 * ファイルパス定数
 *
 * アプリケーション全体で使用するファイルパスを一箇所で管理します。
 */
object FilePaths {
    /**
     * ユーザーホームディレクトリを取得
     */
    private val userHome: String = System.getProperty("user.home")

    /**
     * ベース設定ディレクトリ
     * デフォルト: ~/.local/kinfra
     */
    const val BASE_CONFIG_DIR_NAME = ".local/kinfra"
    val BASE_CONFIG_DIR: String = "$userHome/$BASE_CONFIG_DIR_NAME"

    /**
     * 設定ファイル名
     */
    const val PROJECT_CONFIG_FILE = "project.yaml"
    const val KINFRA_CONFIG_FILE = "kinfra.yaml"

    /**
     * Terraformバックエンド設定ファイル
     */
    const val BACKEND_TFVARS_FILE = "backend.tfvars"

    /**
     * Bitwarden関連ファイル
     */
    const val BW_SESSION_FILE = ".bw_session"
    const val BWS_TOKEN_FILE = ".bws_token"

    /**
     * リポジトリディレクトリ名
     */
    const val REPOS_DIR = "repos"
}
