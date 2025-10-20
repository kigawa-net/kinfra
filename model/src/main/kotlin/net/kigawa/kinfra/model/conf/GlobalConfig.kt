package net.kigawa.kinfra.model.conf

import java.nio.file.Path

interface GlobalConfig {
    val login: LoginConfig?
}

data class LoginConfig(
    val repo: RepositoryName,
    val enabledProjects: List<String> = emptyList(),
    val repoPath: Path = Path.of("")
) {
    companion object {
        fun from(repo: RepositoryName, repoPath: Path, enabledProjects: List<String> = emptyList()): LoginConfig {
            return LoginConfig(
                repo = repo,
                repoPath = repoPath,
                enabledProjects = enabledProjects
            )
        }
    }
}