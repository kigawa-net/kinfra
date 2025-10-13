package net.kigawa.kinfra.model.conf

interface KinfraConfig {
    val project: ProjectInfo
    val terraform: TerraformSettings
    val bitwarden: BitwardenSettings
    val update: UpdateSettings
}

interface ProjectInfo {
    val name: String
    val description: String
}

interface TerraformSettings {
    val version: String
    val workingDirectory: String
}

interface BitwardenSettings {
    val projectId: String
    val useSecretManager: Boolean
}

interface UpdateSettings {
    val autoUpdate: Boolean
    val checkInterval: Long
    val githubRepo: String
}