package net.kigawa.kinfra.infrastructure.config

import net.kigawa.kinfra.model.conf.global.GlobalConfig
import net.kigawa.kinfra.model.conf.global.LoginConfig
import java.nio.file.Path

class GlobalConfigImpl(
    val globalConfigScheme: GlobalConfigScheme,
    val kinfraReposPath: Path,
): GlobalConfig {
    override val login: LoginConfig?
        get() = globalConfigScheme.login?.toLoginConfig(kinfraReposPath)
}