package net.kigawa.kinfra.model.conf

import java.nio.file.Path

interface KinfraConfig {
     val rootProject: ProjectInfo
     val bitwarden: BitwardenSettings?
     val subProjects: List<ProjectInfo>
     val update: UpdateSettings?
     val login: LoginConfig?
 }

interface ProjectInfo {
    val projectId: String
    val description: String?
    val terraform: TerraformSettings?
}

interface TerraformSettings {
    val version: String
    val workingDirectory: String
}

interface BitwardenSettings {
     val projectId: String
 }

interface UpdateSettings {
     val autoUpdate: Boolean
     val checkInterval: Long
     val githubRepo: String
 }

interface LoginConfig {
    val repo: String
    val enabledProjects: List<String>
    val repoPath: Path
}
