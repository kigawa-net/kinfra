package net.kigawa.kinfra.infrastructure.config

import kotlinx.serialization.Serializable
import net.kigawa.kinfra.model.conf.global.GlobalConfig
import net.kigawa.kinfra.model.conf.global.IncompleteGlobalConfig

@Serializable
data class GlobalConfigScheme(
    val login: LoginConfigScheme? = null,
) : IncompleteGlobalConfig {
    fun toGlobalConfig(): GlobalConfig {
        return GlobalConfigImpl(this, java.nio.file.Path.of(""))
    }
    
    companion object {
        fun from(globalConfig: GlobalConfig): GlobalConfigScheme {
            return GlobalConfigScheme(
                login = globalConfig.login?.let { LoginConfigScheme.from(it) }
            )
        }
    }
}