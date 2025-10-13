package net.kigawa.kinfra.model

/**
 * ファイルパス定数
 *
 * アプリケーション全体で使用するファイルパスを一箇所で管理します。
 */
class FilePaths {
    /**
     * ユーザーホームディレクトリを取得
     */
    private val userHome: String = System.getProperty("user.home")

    /**
     * ベース設定ディレクトリ
     * デフォルト: ~/.local/kinfra
     */
     val baseConfigDirName = ".local/kinfra"
    val baseConfigDir: String = "$userHome/$baseConfigDirName"

    /**
     * 設定ファイル名
     */
     val PROJECT_CONFIG_FILE = "project.yaml"
     val KINFRA_CONFIG_FILE = "kinfra.yaml"

    /**
     * Terraformバックエンド設定ファイル
     */
     val BACKEND_TFVARS_FILE = "backend.tfvars"

    /**
     * Bitwarden関連ファイル
     */
     val BW_SESSION_FILE = ".bw_session"
     val BWS_TOKEN_FILE = ".bws_token"

    /**
     * リポジトリディレクトリ名
     */
     val REPOS_DIR = "repos"
}
