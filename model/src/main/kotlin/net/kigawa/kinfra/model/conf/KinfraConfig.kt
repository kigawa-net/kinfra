package net.kigawa.kinfra.model.conf

import net.kigawa.kinfra.model.conf.global.LoginConfig

interface KinfraConfig {
      val rootProject: ProjectInfo
      val bitwarden: BitwardenSettings?
      val subProjects: List<ProjectInfo>
      val update: UpdateSettings?
      @Deprecated("Login configuration should be in GlobalConfig. This property is kept for backward compatibility.")
      val login: LoginConfig?
  }

interface ProjectInfo {
    val projectId: String
    val description: String?
    val terraform: TerraformSettings?
}

interface TerraformVariableMapping {
    val terraformVariable: String
    val bitwardenSecretKey: String
}

interface TerraformSettings {
    val version: String
    val workingDirectory: String
    val variableMappings: List<TerraformVariableMapping>
        get() = emptyList()
    val backendConfig: Map<String, String>
        get() = emptyMap()
}

interface BitwardenSettings {
     val projectId: String
 }

interface UpdateSettings {
     val autoUpdate: Boolean
     val checkInterval: Long
     val githubRepo: String
 }




