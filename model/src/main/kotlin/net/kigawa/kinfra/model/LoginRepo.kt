package net.kigawa.kinfra.model

import net.kigawa.kinfra.model.conf.KinfraConfig
import net.kigawa.kinfra.model.conf.LoginConfig
import java.nio.file.Path

interface LoginRepo {
    val loginConfig: LoginConfig
    fun kinfraConfigPath(): Path
    fun loadKinfraConfig(): KinfraConfig?
    fun saveKinfraConfig(config: KinfraConfig)
    fun kinfraConfigExists(): Boolean
}