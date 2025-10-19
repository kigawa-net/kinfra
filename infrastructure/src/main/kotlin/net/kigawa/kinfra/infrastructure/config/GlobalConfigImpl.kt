package net.kigawa.kinfra.infrastructure.config

import net.kigawa.kinfra.model.conf.GlobalConfig
import net.kigawa.kinfra.model.conf.LoginConfig
import java.nio.file.Path

class GlobalConfigImpl(
    val globalConfigScheme: GlobalConfigScheme,
    val kinfraReposPath: Path,
): GlobalConfig {
    override val login: LoginConfig?
        get() = globalConfigScheme.login
}