package net.kigawa.kinfra.infrastructure.config

import kotlinx.serialization.Serializable
import net.kigawa.kinfra.model.conf.GlobalConfig
import net.kigawa.kinfra.model.conf.LoginConfig

@Serializable
data class LoginConfigScheme(
    override val repo: String,
): LoginConfig

@Serializable
data class GlobalConfigScheme(
    override val login: LoginConfigScheme? = null,
): GlobalConfig {
    companion object {
        fun from(globalConfig: GlobalConfig): GlobalConfigScheme {
            if (globalConfig is GlobalConfigScheme) {
                return globalConfig
            }
            throw IllegalArgumentException(
                "GlobalConfigScheme cannot be converted to a ${GlobalConfigScheme::class.simpleName}"
            )
        }
    }
}