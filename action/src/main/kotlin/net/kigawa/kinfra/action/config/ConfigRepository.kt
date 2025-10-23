package net.kigawa.kinfra.action.config

import net.kigawa.kinfra.model.conf.global.GlobalConfig
import net.kigawa.kinfra.model.conf.KinfraConfig
import net.kigawa.kinfra.model.conf.KinfraParentConfig
import java.nio.file.Path

interface ConfigRepository {
    fun loadGlobalConfig(): GlobalConfig
    fun saveGlobalConfig(config: GlobalConfig)
    fun getProjectConfigFilePath(): String

    fun loadKinfraConfig(filePath: Path): KinfraConfig?
    fun saveKinfraConfig(config: KinfraConfig, filePath: String)
    fun kinfraConfigExists(filePath: String): Boolean

    fun loadKinfraParentConfig(filePath: String): KinfraParentConfig?
    fun saveKinfraParentConfig(config: KinfraParentConfig, filePath: String)
    fun kinfraParentConfigExists(filePath: String): Boolean
}