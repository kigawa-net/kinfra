package net.kigawa.kinfra.infrastructure.config

import kotlinx.serialization.Serializable
import net.kigawa.kinfra.model.conf.GlobalConfig
import net.kigawa.kinfra.model.conf.LoginConfig

@Serializable
data class LoginConfigScheme(
    val repo: String,
    val enabledProjects: List<String> = emptyList(),
) {
    companion object {
        fun from(loginConfig: LoginConfig): LoginConfigScheme {

            return LoginConfigScheme(
                repo = loginConfig.repo,
                enabledProjects = loginConfig.enabledProjects
            )
        }
    }
}

@Serializable
data class GlobalConfigScheme(
    val login: LoginConfigScheme? = null,
) {
    companion object {
        fun from(globalConfig: GlobalConfig): GlobalConfigScheme {
            return GlobalConfigScheme(
                login = globalConfig.login?.let { LoginConfigScheme.from(it) }
            )
        }
    }
}