package net.kigawa.kinfra.infrastructure.config

import kotlinx.serialization.Serializable
import net.kigawa.kinfra.model.conf.*

@Serializable
data class ProjectInfoScheme(
    override val projectId: String = "",
    override val description: String? = null,
    override val terraform: TerraformSettingsScheme? = null
) : ProjectInfo {
    companion object {
        fun from(projectInfo: ProjectInfo): ProjectInfoScheme {
            if (projectInfo is ProjectInfoScheme) {
                return projectInfo
            }
            return ProjectInfoScheme(
                projectId = projectInfo.projectId,
                description = projectInfo.description,
                terraform = projectInfo.terraform?.let { TerraformSettingsScheme.from(it) }
            )
        }
    }
}

@Serializable
data class TerraformSettingsScheme(
    override val version: String = "",
    override val workingDirectory: String = "."
) : TerraformSettings {
    companion object {
        fun from(settings: TerraformSettings): TerraformSettingsScheme {
            if (settings is TerraformSettingsScheme) {
                return settings
            }
            return TerraformSettingsScheme(
                version = settings.version,
                workingDirectory = settings.workingDirectory
            )
        }
    }
    
    fun toTerraformSettings(): TerraformSettings = this
}

@Serializable
data class BitwardenSettingsScheme(
    override val projectId: String = ""
) : BitwardenSettings {
    companion object {
        fun from(settings: BitwardenSettings): BitwardenSettingsScheme {
            if (settings is BitwardenSettingsScheme) {
                return settings
            }
            return BitwardenSettingsScheme(
                projectId = settings.projectId
            )
        }
    }
    
    fun toBitwardenSettings(): BitwardenSettings = this
}

@Serializable
data class UpdateSettingsScheme(
    override val autoUpdate: Boolean = true,
    override val checkInterval: Long = 86400000, // 24 hours in milliseconds
    override val githubRepo: String = "kigawa-net/kinfra"
) : UpdateSettings {
    companion object {
        fun from(settings: UpdateSettings): UpdateSettingsScheme {
            if (settings is UpdateSettingsScheme) {
                return settings
            }
            return UpdateSettingsScheme(
                autoUpdate = settings.autoUpdate,
                checkInterval = settings.checkInterval,
                githubRepo = settings.githubRepo
            )
        }
    }
    
fun toUpdateSettings(): UpdateSettings = this
}

@Serializable
@Deprecated("LoginConfigScheme should be imported from GlobalConfigScheme. This is kept for backward compatibility.")
data class LoginConfigScheme(
    override val repo: String,
    override val enabledProjects: List<String> = emptyList(),
    override val repoPath: java.nio.file.Path
) : LoginConfig {
    companion object {
        fun from(loginConfig: LoginConfig): LoginConfigScheme {
            if (loginConfig is LoginConfigScheme) {
                return loginConfig
            }
            return LoginConfigScheme(
                repo = loginConfig.repo,
                enabledProjects = loginConfig.enabledProjects,
                repoPath = loginConfig.repoPath
            )
        }
    }
}

@Serializable
data class KinfraConfigScheme(
    override val rootProject: ProjectInfoScheme = ProjectInfoScheme(),
    override val bitwarden: BitwardenSettingsScheme? = null,
    override val subProjects: List<ProjectInfoScheme> = emptyList(),
    override val update: UpdateSettingsScheme? = null,
    @Deprecated("Login configuration should be in GlobalConfig. This property is kept for backward compatibility.")
    override val login: LoginConfigScheme? = null
) : KinfraConfig {
    companion object {
        fun from(kinfraConfig: KinfraConfig): KinfraConfigScheme {
            if (kinfraConfig is KinfraConfigScheme) {
                return kinfraConfig
            }
            return KinfraConfigScheme(
                rootProject = ProjectInfoScheme.from(kinfraConfig.rootProject),
                bitwarden = kinfraConfig.bitwarden?.let { BitwardenSettingsScheme.from(it) },
                subProjects = kinfraConfig.subProjects.map { ProjectInfoScheme.from(it) },
                update = kinfraConfig.update?.let { UpdateSettingsScheme.from(it) },
                login = kinfraConfig.login?.let { LoginConfigScheme.from(it) }
            )
        }
    }
}
