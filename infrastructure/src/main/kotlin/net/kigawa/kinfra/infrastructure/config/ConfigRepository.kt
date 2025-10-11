package net.kigawa.kinfra.infrastructure.config

import net.kigawa.kinfra.model.HostsConfig
import net.kigawa.kinfra.model.ProjectConfig
import net.kigawa.kinfra.model.KinfraConfig

interface ConfigRepository {
    fun loadHostsConfig(): HostsConfig
    fun saveHostsConfig(config: HostsConfig)
    fun getConfigFilePath(): String

    fun loadProjectConfig(): ProjectConfig
    fun saveProjectConfig(config: ProjectConfig)
    fun getProjectConfigFilePath(): String

    fun loadKinfraConfig(filePath: String = "kinfra.yaml"): KinfraConfig?
    fun saveKinfraConfig(config: KinfraConfig, filePath: String = "kinfra.yaml")
    fun kinfraConfigExists(filePath: String = "kinfra.yaml"): Boolean
}