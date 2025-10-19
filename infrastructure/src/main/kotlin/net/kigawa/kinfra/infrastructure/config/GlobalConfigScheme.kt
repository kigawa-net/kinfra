package net.kigawa.kinfra.infrastructure.config

import kotlinx.serialization.Serializable
import net.kigawa.kinfra.model.conf.GlobalConfig
import net.kigawa.kinfra.model.conf.LoginConfig



@Serializable
data class GlobalConfigScheme(
    override val login: LoginConfig? = null,
) : GlobalConfig {
    companion object {
        fun from(globalConfig: GlobalConfig): GlobalConfigScheme {
            return GlobalConfigScheme(
                login = globalConfig.login
            )
        }
    }
}