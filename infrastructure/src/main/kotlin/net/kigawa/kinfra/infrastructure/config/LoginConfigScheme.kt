package net.kigawa.kinfra.infrastructure.config

import kotlinx.serialization.Serializable
import net.kigawa.kinfra.model.conf.global.LoginConfig
import net.kigawa.kinfra.model.conf.RepositoryName
import java.nio.file.Path

/**
 * Implementation of LoginConfig interface
 */
data class LoginConfigImpl(
    override val repo: RepositoryName,
    override val enabledProjects: List<String> = emptyList(),
    override val repoPath: Path
) : LoginConfig {
    companion object {
        fun from(repo: RepositoryName, repoPath: Path, enabledProjects: List<String> = emptyList()): LoginConfigImpl {
            return LoginConfigImpl(
                repo = repo,
                repoPath = repoPath,
                enabledProjects = enabledProjects
            )
        }
    }
}

/**
 * Serialization scheme for LoginConfig
 */
@Serializable
data class LoginConfigScheme(
    val repo: String = "",
    val repoPath: String = "",
    val enabledProjects: List<String> = emptyList(),
) {
    fun toLoginConfig(basePath: Path): LoginConfig {
        val resolvedRepoPath = if (repoPath.isNotBlank()) {
            Path.of(repoPath)
        } else {
            basePath.resolve(repo)
        }
        return LoginConfigImpl(
            repo = RepositoryName(repo),
            repoPath = resolvedRepoPath,
            enabledProjects = enabledProjects
        )
    }

    companion object {
        fun from(loginConfig: LoginConfig): LoginConfigScheme {
            return LoginConfigScheme(
                repo = loginConfig.repo.value,
                repoPath = loginConfig.repoPath.toString(),
                enabledProjects = loginConfig.enabledProjects
            )
        }
    }
}