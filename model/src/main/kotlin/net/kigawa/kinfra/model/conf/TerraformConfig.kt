package net.kigawa.kinfra.model.conf

import java.io.File

/**
 * Terraformの設定を表すドメインモデル
 */
data class TerraformConfig(
    val workingDirectory: File,
    val varFile: File?,
    val sshConfigPath: String,
    val backendConfig: Map<String, String> = emptyMap()
) {
    fun hasVarFile(): Boolean = varFile?.exists() == true
}