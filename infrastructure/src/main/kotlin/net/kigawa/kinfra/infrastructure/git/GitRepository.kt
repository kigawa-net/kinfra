package net.kigawa.kinfra.infrastructure.git

import java.io.File

/**
 * Gitリポジトリ情報を取得するユーティリティクラス
 */
object GitRepository {

    /**
     * 現在のディレクトリのGitリポジトリ名を取得
     * リポジトリ名は、リモートURLから取得される（例: https://github.com/user/repo.git -> repo）
     *
     * @param workingDir 作業ディレクトリ（デフォルトはカレントディレクトリ）
     * @return リポジトリ名（取得できない場合はnull）
     */
    fun getRepositoryName(workingDir: File = File(System.getProperty("user.dir"))): String? {
        return try {
            // git remote get-url originでリモートURLを取得
            val process = ProcessBuilder("git", "remote", "get-url", "origin")
                .directory(workingDir)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()

            if (exitCode != 0 || output.isEmpty()) {
                return null
            }

            // URLからリポジトリ名を抽出
            // 例: https://github.com/user/repo.git -> repo
            // 例: git@github.com:user/repo.git -> repo
            extractRepositoryName(output)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Git URLからリポジトリ名を抽出
     *
     * @param url Git URL
     * @return リポジトリ名
     */
    private fun extractRepositoryName(url: String): String {
        // 最後のスラッシュまたはコロンの後の部分を取得
        val name = url.substringAfterLast('/').substringAfterLast(':')

        // .gitサフィックスを削除
        return name.removeSuffix(".git")
    }

    /**
     * 現在のディレクトリがGitリポジトリかどうかを確認
     *
     * @param workingDir 作業ディレクトリ（デフォルトはカレントディレクトリ）
     * @return Gitリポジトリの場合true
     */
    fun isGitRepository(workingDir: File = File(System.getProperty("user.dir"))): Boolean {
        val gitDir = File(workingDir, ".git")
        return gitDir.exists() && gitDir.isDirectory
    }

    /**
     * Gitリポジトリのルートディレクトリを取得
     *
     * @param workingDir 作業ディレクトリ（デフォルトはカレントディレクトリ）
     * @return リポジトリのルートディレクトリ（取得できない場合はnull）
     */
    fun getRepositoryRoot(workingDir: File = File(System.getProperty("user.dir"))): File? {
        return try {
            // git rev-parse --show-toplevelでリポジトリルートを取得
            val process = ProcessBuilder("git", "rev-parse", "--show-toplevel")
                .directory(workingDir)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()

            if (exitCode != 0 || output.isEmpty()) {
                return null
            }

            File(output)
        } catch (e: Exception) {
            null
        }
    }
}
