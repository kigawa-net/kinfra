package net.kigawa.kinfra.infrastructure.config

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import java.io.File

// Kotlin Script support classes
abstract class KinfraParentConfigScript : ScriptWithResult<KinfraParentConfigScheme>

class KinfraParentConfigBuilder {
    var projectName: String = ""
    var description: String? = null
    var terraform: TerraformSettingsScheme? = null
    var subProjects: List<SubProjectScheme> = emptyList()
    var bitwarden: BitwardenSettingsScheme? = null
    var update: UpdateSettingsScheme? = null

    fun terraform(block: TerraformBuilder.() -> Unit) {
        terraform = TerraformBuilder().apply(block).build()
    }

    fun bitwarden(block: BitwardenBuilder.() -> Unit) {
        bitwarden = BitwardenBuilder().apply(block).build()
    }

    fun update(block: UpdateBuilder.() -> Unit) {
        update = UpdateBuilder().apply(block).build()
    }

    fun subProjects(vararg projects: SubProjectScheme) {
        subProjects = projects.toList()
    }

    fun build(): KinfraParentConfigScheme {
        return KinfraParentConfigScheme(
            projectName = projectName,
            description = description,
            terraform = terraform,
            subProjects = subProjects,
            bitwarden = bitwarden,
            update = update
        )
    }
}

class TerraformBuilder {
    var version: String = "1.5.0"
    var workingDirectory: String = "."
    var backendConfig: Map<String, @Contextual Any> = emptyMap()
    var variableMappings: List<TerraformVariableMappingScheme> = emptyList()
    var outputMappings: List<TerraformOutputMappingScheme> = emptyList()
    var generateOutputDir: String? = null

    fun backendConfig(vararg pairs: Pair<String, Any>) {
        backendConfig = mapOf(*pairs)
    }

    fun variableMappings(vararg mappings: TerraformVariableMappingScheme) {
        variableMappings = mappings.toList()
    }

    fun outputMappings(vararg mappings: TerraformOutputMappingScheme) {
        outputMappings = mappings.toList()
    }

    fun build(): TerraformSettingsScheme {
        return TerraformSettingsScheme(
            version = version,
            workingDirectory = workingDirectory,
            backendConfig = backendConfig,
            variableMappings = variableMappings,
            outputMappings = outputMappings,
            generateOutputDir = generateOutputDir
        )
    }
}

class BitwardenBuilder {
    var projectId: String = ""

    fun build(): BitwardenSettingsScheme {
        return BitwardenSettingsScheme(projectId = projectId)
    }
}

class UpdateBuilder {
    var autoUpdate: Boolean = true
    var checkInterval: Long = 86400000L
    var githubRepo: String = "kigawa-net/kinfra"

    fun build(): UpdateSettingsScheme {
        return UpdateSettingsScheme(
            autoUpdate = autoUpdate,
            checkInterval = checkInterval,
            githubRepo = githubRepo
        )
    }
}
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

     override fun addSubProject(name: String, path: String): SubProject {
         return SubProjectImpl(name, path)
     }
    
    companion object {
        fun fromFile(file: File): KinfraParentConfigImpl {
            return when (file.extension.lowercase()) {
                "kts" -> fromKtsFile(file)
                else -> fromYamlFile(file)
            }
        }

        private fun fromYamlFile(file: File): KinfraParentConfigImpl {
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

        private fun fromKtsFile(file: File): KinfraParentConfigImpl {
            try {
                val scriptSource = file.toScriptSource()
                val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<KinfraParentConfigScript>()

                val evaluationConfiguration = ScriptEvaluationConfiguration {
                    implicitReceivers(KinfraParentConfigBuilder::class)
                }

                val result = BasicJvmScriptingHost().eval(scriptSource, compilationConfiguration, evaluationConfiguration)

                return when (result) {
                    is ResultWithDiagnostics.Success -> {
                        val scriptResult = result.value.returnValue
                        when (scriptResult) {
                            is ResultValue.Value -> {
                                val config = scriptResult.value as KinfraParentConfigScheme
                                KinfraParentConfigImpl(config, file)
                            }
                            is ResultValue.Error -> {
                                throw IllegalArgumentException("Script evaluation failed: ${scriptResult.error}")
                            }
                            else -> {
                                throw IllegalArgumentException("Script did not return a configuration object")
                            }
                        }
                    }
                    is ResultWithDiagnostics.Failure -> {
                        val errors = result.reports.joinToString("\n") { it.message }
                        throw IllegalArgumentException("Failed to evaluate Kotlin script: $errors")
                    }
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to parse Kotlin script config file", e)
            }
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