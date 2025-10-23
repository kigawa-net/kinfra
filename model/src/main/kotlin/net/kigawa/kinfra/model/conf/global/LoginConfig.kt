package net.kigawa.kinfra.model.conf.global

import net.kigawa.kinfra.model.conf.RepositoryName
import java.nio.file.Path

interface LoginConfig {
    val repo: RepositoryName
    val enabledProjects: List<String>
    val repoPath: Path
}