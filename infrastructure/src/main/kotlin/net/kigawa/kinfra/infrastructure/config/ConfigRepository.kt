package net.kigawa.kinfra.infrastructure.config

import net.kigawa.kinfra.model.GlobalConfig
import net.kigawa.kinfra.model.KinfraConfig
import net.kigawa.kinfra.model.FilePaths

interface ConfigRepository {
    fun loadGlobalConfig(): GlobalConfig
    fun saveGlobalConfig(config: GlobalConfig)
    fun getProjectConfigFilePath(): String

    fun loadKinfraConfig(filePath: String ): KinfraConfig?
    fun saveKinfraConfig(config: KinfraConfig, filePath: String)
    fun kinfraConfigExists(filePath: String ): Boolean
}