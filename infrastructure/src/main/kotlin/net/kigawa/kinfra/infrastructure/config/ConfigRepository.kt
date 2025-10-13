package net.kigawa.kinfra.infrastructure.config

import net.kigawa.kinfra.model.GlobalConfig
import net.kigawa.kinfra.model.KinfraConfig
import net.kigawa.kinfra.model.FilePaths

interface ConfigRepository {
    fun loadGlobalConfig(): GlobalConfig
    fun saveGlobalConfig(config: GlobalConfig)
    fun getProjectConfigFilePath(): String

    fun loadKinfraConfig(filePath: String = FilePaths.KINFRA_CONFIG_FILE): KinfraConfig?
    fun saveKinfraConfig(config: KinfraConfig, filePath: String = FilePaths.KINFRA_CONFIG_FILE)
    fun kinfraConfigExists(filePath: String = FilePaths.KINFRA_CONFIG_FILE): Boolean
}