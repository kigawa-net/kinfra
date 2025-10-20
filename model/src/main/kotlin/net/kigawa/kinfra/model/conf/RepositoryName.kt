package net.kigawa.kinfra.model.conf

/**
 * リポジトリ名を表すクラス
 * 例: "kigawa01/infra"
 */
data class RepositoryName(val value: String) {
    init {
        require(value.isNotBlank()) { "Repository name cannot be blank" }
    }

    /**
     * リポジトリ名の最後の部分を取得（スラッシュの後）
     * 例: "kigawa01/infra" -> "infra"
     */
    fun getShortName(): String = value.substringAfterLast('/')

    override fun toString(): String = value

    companion object {
        /**
         * GitHub リポジトリURLまたはパスから RepositoryName を作成
         * @param githubRepo GitHub repository in various formats:
         *   - user/repo
         *   - https://github.com/user/repo.git
         *   - git@github.com:user/repo.git
         * @return RepositoryName or null if invalid format
         */
        fun fromGitHubRepo(githubRepo: String): RepositoryName? {
            val repoName = when {
                // HTTPS URL: https://github.com/user/repo.git
                githubRepo.startsWith("https://github.com/") -> {
                    githubRepo.removePrefix("https://github.com/").removeSuffix(".git")
                }
                // SSH URL: git@github.com:user/repo.git
                githubRepo.startsWith("git@github.com:") -> {
                    githubRepo.removePrefix("git@github.com:").removeSuffix(".git")
                }
                // Short format: user/repo
                githubRepo.matches(Regex("^[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+$")) -> {
                    githubRepo
                }
                else -> return null
            }

            return RepositoryName(repoName)
        }
    }
}
