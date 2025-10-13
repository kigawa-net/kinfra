package net.kigawa.kinfra.model.conf

import java.nio.file.Path

/**
 * ファイルパス定数
 *
 * アプリケーション全体で使用するファイルパスを一箇所で管理します。
 *
 * @param homeDirGetter ホームディレクトリを取得するための実装
 */
class FilePaths(
    private val homeDirGetter: HomeDirGetter
) {
    /**
     * ユーザーホームディレクトリを取得
     */
    private val userHome: Path get() = homeDirGetter.getHomeDir()

    /**
     * ベース設定ディレクトリ
     * デフォルト: ~/.local/kinfra
     */
     val baseConfigDirName = ".local/kinfra"
    val baseConfigDir: Path? = userHome.resolve(baseConfigDirName).toAbsolutePath()

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