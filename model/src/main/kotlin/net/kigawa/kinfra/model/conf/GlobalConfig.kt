package net.kigawa.kinfra.model.conf

import java.nio.file.Path

interface GlobalConfig {
    val login: LoginConfig?
}

interface LoginConfig {
    val repo: String
    val enabledProjects: List<String>
    val repoPath: Path?
}