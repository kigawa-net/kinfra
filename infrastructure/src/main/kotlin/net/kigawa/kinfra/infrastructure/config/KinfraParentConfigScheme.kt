package net.kigawa.kinfra.infrastructure.config

import kotlinx.serialization.Serializable
import net.kigawa.kinfra.model.conf.KinfraParentConfig
import net.kigawa.kinfra.model.conf.KinfraParentConfigData
import net.kigawa.kinfra.model.conf.SubProject

/**
 * Serializable implementation of KinfraParentConfig
 */
@Serializable
data class KinfraParentConfigScheme(
    val projectName: String = "",
    val description: String? = null,
    val terraform: TerraformSettingsScheme? = null,
    val subProjects: List<SubProjectScheme> = emptyList(),
    val bitwarden: BitwardenSettingsScheme? = null,
    val update: UpdateSettingsScheme? = null,
) {
    companion object {
        fun from(config: KinfraParentConfig): KinfraParentConfigScheme {
            return KinfraParentConfigScheme(
                projectName = config.projectName,
                description = config.description,
                terraform = config.terraform?.let { TerraformSettingsScheme.from(it) },
                subProjects = config.subProjects.map { SubProjectScheme.from(it) },
                bitwarden = config.bitwarden?.let { BitwardenSettingsScheme.from(it) },
                update = config.update?.let { UpdateSettingsScheme.from(it) }
            )
        }

        fun from(config: KinfraParentConfigData): KinfraParentConfigScheme {
            return KinfraParentConfigScheme(
                projectName = config.projectName,
                description = config.description,
                terraform = config.terraform?.let { TerraformSettingsScheme.from(it) },
                subProjects = config.subProjects.map { SubProjectScheme.from(it) },
                bitwarden = config.bitwarden?.let { BitwardenSettingsScheme.from(it) },
                update = config.update?.let { UpdateSettingsScheme.from(it) }

            )
        }

        /**
         * 後方互換性のため、文字列形式のYAMLから変換する
         */
        fun fromStringList(subProjects: List<String>): List<SubProjectScheme> {
            return subProjects.map { SubProjectScheme.from(SubProject.fromString(it)) }
        }
    }
}
