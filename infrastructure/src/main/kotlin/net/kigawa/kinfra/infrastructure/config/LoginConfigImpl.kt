package net.kigawa.kinfra.infrastructure.config

import net.kigawa.kinfra.model.conf.LoginConfig
import java.nio.file.Path

class LoginConfigImpl(
    val loginConfigScheme: LoginConfigScheme,
    val kinfraReposPath: Path,
): LoginConfig {
    override val repo: String
        get() = loginConfigScheme.repo
    override val enabledProjects: List<String>
        get() = loginConfigScheme.enabledProjects
    override val repoPath: Path
        get() = kinfraReposPath.resolve(repo)
}