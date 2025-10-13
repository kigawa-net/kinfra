package net.kigawa.kinfra.infrastructure.config

import net.kigawa.kinfra.model.conf.GlobalConfig
import net.kigawa.kinfra.model.conf.KinfraConfig

interface ConfigRepository {
    fun loadGlobalConfig(): GlobalConfig
    fun saveGlobalConfig(config: GlobalConfig)
    fun getProjectConfigFilePath(): String

    fun loadKinfraConfig(filePath: String ): KinfraConfig?
    fun saveKinfraConfig(config: KinfraConfig, filePath: String)
    fun kinfraConfigExists(filePath: String ): Boolean
}