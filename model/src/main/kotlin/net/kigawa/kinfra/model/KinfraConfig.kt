package net.kigawa.kinfra.model

import kotlinx.serialization.Serializable

@Serializable
data class KinfraConfig(
    val project: ProjectInfo = ProjectInfo(),
    val terraform: TerraformSettings = TerraformSettings(),
    val bitwarden: BitwardenSettings = BitwardenSettings(),
    val update: UpdateSettings = UpdateSettings()
)

@Serializable
data class ProjectInfo(
    val name: String = "",
    val repository: String = "",
    val description: String = ""
)

@Serializable
data class TerraformSettings(
    val version: String = "",
    val workingDirectory: String = "terraform"
)

@Serializable
data class BitwardenSettings(
    val projectId: String = "",
    val useSecretManager: Boolean = true
)

@Serializable
data class UpdateSettings(
    val autoUpdate: Boolean = true,
    val checkInterval: Long = 86400000, // 24 hours in milliseconds
    val githubRepo: String = "kigawa-net/kinfra"
)