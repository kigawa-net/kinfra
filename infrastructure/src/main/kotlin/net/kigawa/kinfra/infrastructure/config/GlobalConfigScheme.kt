package net.kigawa.kinfra.infrastructure.config

import kotlinx.serialization.Serializable
import net.kigawa.kinfra.model.conf.GlobalConfig
import net.kigawa.kinfra.model.conf.LoginConfig
import java.nio.file.Path

@Serializable
data class LoginConfigScheme(
    override val repo: String = "",
    override val enabledProjects: List<String> = emptyList(),
    override val repoPath: Path = Path.of("")
) : LoginConfig {
    companion object {
        fun from(loginConfig: LoginConfig): LoginConfigScheme {
            if (loginConfig is LoginConfigScheme) {
                return loginConfig
            }
            return LoginConfigScheme(
                repo = loginConfig.repo,
                enabledProjects = loginConfig.enabledProjects,
                repoPath = loginConfig.repoPath
            )
        }
    }
}

@Serializable
data class GlobalConfigScheme(
    override val login: LoginConfig? = null,
) : GlobalConfig {
    companion object {
        fun from(globalConfig: GlobalConfig): GlobalConfigScheme {
            return GlobalConfigScheme(
                login = globalConfig.login?.let { LoginConfigScheme.from(it) }
            )
        }
    }
}