package net.kigawa.kinfra.infrastructure.config

import kotlinx.serialization.Serializable
import net.kigawa.kinfra.model.conf.*

/**
 * Serializable implementation of KinfraParentConfig
 */
@Serializable
data class KinfraParentConfigScheme(
    override val projectName: String = "",
    override val description: String? = null,
    override val terraform: TerraformSettingsScheme? = null,
    override val subProjects: List<String> = emptyList(),
    override val bitwarden: BitwardenSettingsScheme? = null,
    override val update: UpdateSettingsScheme? = null
) : KinfraParentConfig {
    companion object {
        fun from(config: KinfraParentConfig): KinfraParentConfigScheme {
            if (config is KinfraParentConfigScheme) {
                return config
            }
            return KinfraParentConfigScheme(
                projectName = config.projectName,
                description = config.description,
                terraform = config.terraform?.let { TerraformSettingsScheme.from(it) },
                subProjects = config.subProjects,
                bitwarden = config.bitwarden?.let { BitwardenSettingsScheme.from(it) },
                update = config.update?.let { UpdateSettingsScheme.from(it) }
            )
        }
    }
}
