package net.kigawa.kinfra.model

import net.kigawa.kinfra.model.conf.KinfraConfig
import net.kigawa.kinfra.model.conf.KinfraParentConfig
import net.kigawa.kinfra.model.conf.KinfraParentConfigData
import net.kigawa.kinfra.model.conf.global.LoginConfig
import java.nio.file.Path

interface LoginRepo {
    val repoPath: Path
    val loginConfig: LoginConfig
    fun kinfraConfigPath(): Path
    fun loadKinfraConfig(): KinfraConfig?
    fun saveKinfraConfig(config: KinfraConfig)
    fun kinfraConfigExists(): Boolean
    fun loadKinfraBaseConfig(): KinfraParentConfig?
    fun createKinfraParentConfig(kinfraParentConfigData: KinfraParentConfigData): KinfraParentConfig
    fun kinfraBaseConfigPath(): Path
}