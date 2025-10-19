package net.kigawa.kinfra.infrastructure.config

import net.kigawa.kinfra.model.conf.LoginConfig
import java.nio.file.Path

class LoginConfigImpl(
    private val loginConfig: LoginConfig,
    val kinfraReposPath: Path,
): LoginConfig {
    override val repo: String
        get() = loginConfig.repo
    override val enabledProjects: List<String>
        get() = loginConfig.enabledProjects
    override val repoPath: Path
        get() = loginConfig.repoPath ?: kinfraReposPath.resolve(loginConfig.repo).resolve("repo")
}