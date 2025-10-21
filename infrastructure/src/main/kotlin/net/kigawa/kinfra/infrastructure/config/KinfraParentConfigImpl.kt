package net.kigawa.kinfra.infrastructure.config

import com.charleskorn.kaml.Yaml
import net.kigawa.kinfra.model.sub.SubProject
import net.kigawa.kinfra.model.conf.*
import java.io.File
import java.nio.file.Path

class KinfraParentConfigImpl(
    private var kinfraParentConfigScheme: KinfraParentConfigScheme,
    val file: File,
): KinfraParentConfig {
    override val projectName: String
        get() = kinfraParentConfigScheme.projectName
    override val description: String?
        get() = kinfraParentConfigScheme.description
    override val terraform: TerraformSettings?
        get() = kinfraParentConfigScheme.terraform?.toTerraformSettings()
    override val subProjects: List<SubProject>
        get() = kinfraParentConfigScheme.subProjects.map { it.toSubProject() }
    override val bitwarden: BitwardenSettings?
        get() = kinfraParentConfigScheme.bitwarden?.toBitwardenSettings()
    override val update: UpdateSettings?
        get() = kinfraParentConfigScheme.update?.toUpdateSettings()
    override val filePath: Path
        get() = file.toPath()

    override fun toData(): KinfraParentConfigData {
        return KinfraParentConfigData(
            projectName = projectName,
            description = description,
            terraform = terraform,
            subProjects = subProjects,
            bitwarden = bitwarden,
            update = update,
        )
    }

    override fun saveData(updatedConfig: KinfraParentConfigData) {
        kinfraParentConfigScheme = KinfraParentConfigScheme.from(updatedConfig)
        file.writeText(
            Yaml.default.encodeToString(
                KinfraParentConfigScheme.serializer(), kinfraParentConfigScheme
            )
        )
    }
    
    companion object {
        /**
         * 後方互換性のためのファクトリメソッド
         * 文字列形式とオブジェクト形式の両方のYAMLを読み込める
         */
        fun fromFile(file: File): KinfraParentConfigImpl {
            val content = file.readText()
            
            return try {
                // まずオブジェクト形式としてデコードを試行
                val config = Yaml.default.decodeFromString(KinfraParentConfigScheme.serializer(), content)
                KinfraParentConfigImpl(config, file)
            } catch (e: Exception) {
                // 失敗した場合は、文字列形式の古いYAMLとして処理
                try {
                    val legacyConfig = Yaml.default.decodeFromString(LegacyKinfraParentConfigScheme.serializer(), content)
                    val newConfig = KinfraParentConfigScheme(
                        projectName = legacyConfig.projectName,
                        description = legacyConfig.description,
                        terraform = legacyConfig.terraform?.let { TerraformSettingsScheme.from(it.toTerraformSettings()) },
                        subProjects = KinfraParentConfigScheme.fromStringList(legacyConfig.subProjects),
                        bitwarden = legacyConfig.bitwarden?.let { BitwardenSettingsScheme.from(it.toBitwardenSettings()) },
                        update = legacyConfig.update?.let { UpdateSettingsScheme.from(it.toUpdateSettings()) }
                    )
                    KinfraParentConfigImpl(newConfig, file)
                } catch (e2: Exception) {
                    throw IllegalArgumentException("Failed to parse config file as either new or legacy format", e2)
                }
            }
        }
    }
}

/**
 * レガシー形式の設定（文字列リストのsubProjectsを持つ）
 */
@kotlinx.serialization.Serializable
data class LegacyKinfraParentConfigScheme(
    val projectName: String = "",
    val description: String? = null,
    val terraform: TerraformSettingsScheme? = null,
    val subProjects: List<String> = emptyList(),
    val bitwarden: BitwardenSettingsScheme? = null,
    val update: UpdateSettingsScheme? = null,
)