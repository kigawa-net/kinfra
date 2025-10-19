package net.kigawa.kinfra.infrastructure.config

import kotlinx.serialization.Serializable
import net.kigawa.kinfra.model.conf.GlobalConfig
import net.kigawa.kinfra.model.conf.LoginConfig

@Serializable
data class LoginConfigScheme(
    override val repo: String,
    override val enabledProjects: List<String> = emptyList()
): LoginConfig {
    companion object {
        fun from(loginConfig: LoginConfig): LoginConfigScheme {
            if (loginConfig is LoginConfigScheme) {
                return loginConfig
            }
            return LoginConfigScheme(
                repo = loginConfig.repo,
                enabledProjects = loginConfig.enabledProjects
            )
        }
    }
}

@Serializable
data class GlobalConfigScheme(
    override val login: LoginConfigScheme? = null,
): GlobalConfig {
    companion object {
        fun from(globalConfig: GlobalConfig): GlobalConfigScheme {
            if (globalConfig is GlobalConfigScheme) {
                return globalConfig
            }
            return GlobalConfigScheme(
                login = globalConfig.login?.let { LoginConfigScheme.from(it) }
            )
        }
    }
}