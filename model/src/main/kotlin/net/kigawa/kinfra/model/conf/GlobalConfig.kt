package net.kigawa.kinfra.model.conf

import java.nio.file.Path

interface GlobalConfig {
    val login: LoginConfig?
}

interface LoginConfig {
    val repo: RepositoryName
    val enabledProjects: List<String>
    val repoPath: Path
}