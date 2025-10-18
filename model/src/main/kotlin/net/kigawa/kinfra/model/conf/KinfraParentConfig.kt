package net.kigawa.kinfra.model.conf

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
     * List of sub-project paths or identifiers
     */
    val subProjects: List<String>

    /**
     * Common Bitwarden settings
     */
    val bitwarden: BitwardenSettings?

    /**
     * Update settings for the parent project
     */
    val update: UpdateSettings?
}
