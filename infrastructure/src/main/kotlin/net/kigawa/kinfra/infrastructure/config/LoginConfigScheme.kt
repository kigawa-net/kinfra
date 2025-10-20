package net.kigawa.kinfra.infrastructure.config

import kotlinx.serialization.Serializable
import net.kigawa.kinfra.model.conf.LoginConfig
import net.kigawa.kinfra.model.conf.RepositoryName
import java.nio.file.Path

/**
 * Serialization scheme for LoginConfig
 */
@Serializable
data class LoginConfigScheme(
    val repo: String = "",
    val enabledProjects: List<String> = emptyList(),
    val repoPath: String = ""
) {
    fun toLoginConfig(): LoginConfig {
        return LoginConfig(
            repo = RepositoryName(repo),
            repoPath = Path.of(repoPath),
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