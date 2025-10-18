package net.kigawa.kinfra.infrastructure.config

import kotlinx.serialization.Serializable
import net.kigawa.kinfra.model.conf.*

@Serializable
data class ProjectInfoScheme(
    override val projectId: String = "",
    override val description: String = "",
    override val terraform: TerraformSettingsScheme? = null
) : ProjectInfo

@Serializable
data class TerraformSettingsScheme(
    override val version: String = "",
    override val workingDirectory: String = "terraform"
) : TerraformSettings

@Serializable
data class BitwardenSettingsScheme(
    override val projectId: String = ""
) : BitwardenSettings

@Serializable
data class UpdateSettingsScheme(
    override val autoUpdate: Boolean = true,
    override val checkInterval: Long = 86400000, // 24 hours in milliseconds
    override val githubRepo: String = "kigawa-net/kinfra"
) : UpdateSettings

@Serializable
data class KinfraConfigScheme(
    override val rootProject: ProjectInfoScheme = ProjectInfoScheme(),
    override val bitwarden: BitwardenSettingsScheme? = null,
    override val subProjects: List<ProjectInfoScheme> = emptyList(),
    override val update: UpdateSettingsScheme? = null
) : KinfraConfig {
    companion object {
        fun from(kinfraConfig: KinfraConfig): KinfraConfigScheme {
            if (kinfraConfig is KinfraConfigScheme) {
                return kinfraConfig
            }
            throw IllegalArgumentException(
                "KinfraConfig cannot be converted to a ${KinfraConfigScheme::class.simpleName}"
            )
        }
    }
}
