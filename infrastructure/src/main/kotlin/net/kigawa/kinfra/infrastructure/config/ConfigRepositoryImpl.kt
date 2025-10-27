package net.kigawa.kinfra.infrastructure.config

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import net.kigawa.kinfra.model.conf.*
import java.io.File
import java.nio.file.Path

// Kotlin Script support classes for kinfra.yaml
abstract class KinfraConfigScript : ScriptWithResult<KinfraConfigScheme>

class KinfraConfigBuilder {
    var project: ProjectSettingsScheme? = null
    var rootProject: ProjectSettingsScheme? = null
    var subProjects: List<SubProjectScheme> = emptyList()
    var bitwarden: BitwardenSettingsScheme? = null
    var update: UpdateSettingsScheme? = null

    fun project(block: ProjectBuilder.() -> Unit) {
        project = ProjectBuilder().apply(block).build()
    }

    fun rootProject(block: ProjectBuilder.() -> Unit) {
        rootProject = ProjectBuilder().apply(block).build()
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

    fun build(): KinfraConfigScheme {
        return KinfraConfigScheme(
            project = project,
            rootProject = rootProject,
            subProjects = subProjects,
            bitwarden = bitwarden,
            update = update
        )
    }
}

class ProjectBuilder {
    var projectId: String = ""
    var name: String = ""
    var description: String? = null
    var terraform: TerraformSettingsScheme? = null

    fun terraform(block: TerraformBuilder.() -> Unit) {
        terraform = TerraformBuilder().apply(block).build()
    }

    fun build(): ProjectSettingsScheme {
        return ProjectSettingsScheme(
            projectId = projectId,
            name = name,
            description = description,
            terraform = terraform
        )
    }
}

/**
 * 設定ファイルを操作する実装です。
 */
class ConfigRepositoryImpl(
    private val filePaths: FilePaths,
    private val logger: Logger,
    private val globalConfigCompleter: GlobalConfigCompleter
) : ConfigRepository {
    private val yaml = Yaml(
        configuration = YamlConfiguration(
            encodeDefaults = false,
            strictMode = false
        )
    )

    // 基本設定ディレクトリ
    private val configDir get() = filePaths.baseConfigDir?.toFile()
        ?: throw IllegalStateException("Config directory not available")

    // project.yaml の場所
    private val projectFile get() = File(configDir, filePaths.projectConfigFileName)

    init { ensureConfigDirExists() }

    private fun ensureConfigDirExists() { if (!configDir.exists()) configDir.mkdirs() }

    override fun loadGlobalConfig(): GlobalConfig {
        val reposPath = filePaths.baseConfigDir?.resolve(filePaths.reposDir)
            ?: throw IllegalStateException("Config directory not available")
        return if (projectFile.exists()) {
            try {
                val yaml = projectFile.readText()
                val scheme = Yaml.default.decodeFromString(GlobalConfigScheme.serializer(), yaml)

                // GlobalConfigCompleter を使って設定を補完
                val completedConfig = globalConfigCompleter.complete(scheme)

                // 設定が変更された場合は保存
                if (completedConfig is GlobalConfigImpl && completedConfig.globalConfigScheme != scheme) {
                    saveGlobalConfig(completedConfig)
                    logger.info("設定ファイルを更新しました")
                }

                completedConfig
            } catch (e: Exception) {
                logger.debug("設定ファイルの読み込みに失敗: ${e.message}")
                val reposPath = filePaths.baseConfigDir?.resolve(filePaths.reposDir)
                    ?: throw IllegalStateException("Config directory not available")
                GlobalConfigImpl(GlobalConfigScheme(), reposPath)
            }
        } else {
            val reposPath = filePaths.baseConfigDir?.resolve(filePaths.reposDir)
                ?: throw IllegalStateException("Config directory not available")
            GlobalConfigImpl(GlobalConfigScheme(), reposPath)
        }
    }

    override fun saveGlobalConfig(config: GlobalConfig) {
        val yaml = Yaml.default.encodeToString(GlobalConfigScheme.serializer(), GlobalConfigScheme.from(config))
        projectFile.writeText(yaml)
    }

    override fun getProjectConfigFilePath() = projectFile.absolutePath

    override fun loadKinfraConfig(filePath: Path): KinfraConfig? {
        val file = filePath.toFile()
        if (!file.exists()) return null

        return when (file.extension.lowercase()) {
            "kts" -> loadKinfraConfigFromKts(file)
            else -> loadKinfraConfigFromYaml(file)
        }
    }

    private fun loadKinfraConfigFromYaml(file: File): KinfraConfig {
        return yaml.decodeFromString(KinfraConfigScheme.serializer(), file.readText())
    }

    private fun loadKinfraConfigFromKts(file: File): KinfraConfig {
        try {
            val scriptSource = file.toScriptSource()
            val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<KinfraConfigScript>()

            val evaluationConfiguration = ScriptEvaluationConfiguration {
                implicitReceivers(KinfraConfigBuilder::class)
            }

            val result = BasicJvmScriptingHost().eval(scriptSource, compilationConfiguration, evaluationConfiguration)

            return when (result) {
                is ResultWithDiagnostics.Success -> {
                    val scriptResult = result.value.returnValue
                    when (scriptResult) {
                        is ResultValue.Value -> {
                            scriptResult.value as KinfraConfigScheme
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

    override fun saveKinfraConfig(config: KinfraConfig, filePath: String) {
        val file = File(filePath)
        val yaml = Yaml.default.encodeToString(KinfraConfigScheme.serializer(), KinfraConfigScheme.from(config))
        file.writeText(yaml)
    }

    override fun kinfraConfigExists(filePath: String) = File(filePath).exists()

    override fun saveKinfraParentConfig(config: KinfraParentConfig, filePath: String) {
        val file = File(filePath)
        val yaml = Yaml.default.encodeToString(KinfraParentConfigScheme.serializer(), KinfraParentConfigScheme.from(config))
        file.writeText(yaml)
    }

    override fun loadKinfraParentConfig(filePath: String): KinfraParentConfig? {
        val file = File(filePath)
        return if (file.exists()) {
            KinfraParentConfigImpl.fromFile(file)
        } else null
    }

    override fun kinfraParentConfigExists(filePath: String) = File(filePath).exists()
}
