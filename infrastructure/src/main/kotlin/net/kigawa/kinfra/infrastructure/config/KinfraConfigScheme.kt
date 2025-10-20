package net.kigawa.kinfra.infrastructure.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kigawa.kinfra.model.conf.*

@Serializable
data class ProjectInfoScheme(
    private val projectIdField: String? = null,
    override val description: String? = null,
    override val terraform: TerraformSettingsScheme? = null,
    // Backward compatibility for old 'name' property
    private val name: String? = null
) : ProjectInfo {

    override val projectId: String
        get() = projectIdField ?: name ?: ""
    companion object {
        fun from(projectInfo: ProjectInfo): ProjectInfoScheme {
            if (projectInfo is ProjectInfoScheme) {
                return projectInfo
            }
            return ProjectInfoScheme(
                projectIdField = projectInfo.projectId,
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
data class KinfraConfigScheme(
    private val rootProjectField: ProjectInfoScheme? = null,
    override val bitwarden: BitwardenSettingsScheme? = null,
    override val subProjects: List<ProjectInfoScheme> = emptyList(),
    override val update: UpdateSettingsScheme? = null,
    @Deprecated("Login configuration should be in GlobalConfig. This property is kept for backward compatibility.")
    @Transient
    private val loginScheme: LoginConfigScheme? = null,
    // Backward compatibility for old 'project' property - save as 'project' for compatibility
    private val project: ProjectInfoScheme? = null
) : KinfraConfig {

    override val rootProject: ProjectInfoScheme
        get() = project ?: rootProjectField ?: ProjectInfoScheme()

    @Deprecated("Login configuration should be in GlobalConfig. This property is kept for backward compatibility.")
    override val login: LoginConfig?
        get() = loginScheme?.toLoginConfig()
    companion object {
        fun from(kinfraConfig: KinfraConfig): KinfraConfigScheme {
            if (kinfraConfig is KinfraConfigScheme) {
                return kinfraConfig
            }
            return KinfraConfigScheme(
                project = ProjectInfoScheme.from(kinfraConfig.rootProject),
                bitwarden = kinfraConfig.bitwarden?.let { BitwardenSettingsScheme.from(it) },
                subProjects = kinfraConfig.subProjects.map { ProjectInfoScheme.from(it) },
                update = kinfraConfig.update?.let { UpdateSettingsScheme.from(it) },
                loginScheme = kinfraConfig.login?.let { LoginConfigScheme.from(it) }
            )
        }
    }
}
