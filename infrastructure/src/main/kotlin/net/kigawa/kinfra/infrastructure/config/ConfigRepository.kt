package net.kigawa.kinfra.infrastructure.config

import net.kigawa.kinfra.model.HostsConfig
import net.kigawa.kinfra.model.ProjectConfig

interface ConfigRepository {
    fun loadHostsConfig(): HostsConfig
    fun saveHostsConfig(config: HostsConfig)
    fun getConfigFilePath(): String

    fun loadProjectConfig(): ProjectConfig
    fun saveProjectConfig(config: ProjectConfig)
    fun getProjectConfigFilePath(): String
}