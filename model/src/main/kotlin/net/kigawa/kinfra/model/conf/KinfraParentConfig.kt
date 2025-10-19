package net.kigawa.kinfra.model.conf

import java.nio.file.Path

/**
 * Kinfra parent project configuration
 * Used for managing common settings across multiple sub-projects
 */
interface KinfraParentConfig {
    /**
     * Parent project name
     */
    val projectName: String

    /**
     * Parent project description
     */
    val description: String?

    /**
     * Common Terraform settings for all sub-projects
     */
    val terraform: TerraformSettings?

    /**
     * List of sub-projects
     */
    val subProjects: List<SubProject>

    /**
     * Common Bitwarden settings
     */
    val bitwarden: BitwardenSettings?

    /**
     * Update settings for the parent project
     */
    val update: UpdateSettings?
    val filePath: Path
    fun toData(): KinfraParentConfigData
    fun saveData(updatedConfig: KinfraParentConfigData)
}
