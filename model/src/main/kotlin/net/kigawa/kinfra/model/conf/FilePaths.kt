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
     val projectConfigFileName = "project.yaml"
     val kinfraConfigFileName = "kinfra.yaml"
     val kinfraParentConfigFileName = "kinfra-parent.yaml"

    /**
     * Bitwarden関連ファイル
     */
     val bwSessionFileName = ".bw_session"
     val bwsTokenFileName = ".bws_token"

    /**
     * リポジトリディレクトリ名
     */
     val reposDir = "repos"
}