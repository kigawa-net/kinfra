package net.kigawa.kinfra.infrastructure.config

import kotlinx.serialization.Serializable
import net.kigawa.kinfra.model.conf.*

@Serializable
data class ProjectInfoScheme(
    override val name: String = "",
    override val description: String = ""
) : ProjectInfo

@Serializable
data class TerraformSettingsScheme(
    override val version: String = "",
    override val workingDirectory: String = "terraform"
) : TerraformSettings

@Serializable
data class BitwardenSettingsScheme(
    override val projectId: String = "",
    override val useSecretManager: Boolean = true
) : BitwardenSettings

@Serializable
data class UpdateSettingsScheme(
    override val autoUpdate: Boolean = true,
    override val checkInterval: Long = 86400000, // 24 hours in milliseconds
    override val githubRepo: String = "kigawa-net/kinfra"
) : UpdateSettings

@Serializable
data class KinfraConfigScheme(
    override val project: ProjectInfoScheme = ProjectInfoScheme(),
    override val terraform: TerraformSettingsScheme = TerraformSettingsScheme(),
    override val bitwarden: BitwardenSettingsScheme = BitwardenSettingsScheme(),
    override val update: UpdateSettingsScheme = UpdateSettingsScheme()
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
