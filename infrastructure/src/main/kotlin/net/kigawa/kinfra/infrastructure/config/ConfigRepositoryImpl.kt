package net.kigawa.kinfra.infrastructure.config

import com.charleskorn.kaml.Yaml
import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.conf.GlobalConfig
import net.kigawa.kinfra.model.conf.KinfraConfig
import net.kigawa.kinfra.model.conf.KinfraParentConfig
import java.io.File

class ConfigRepositoryImpl(
    val filePaths: FilePaths,
    val globalConfigScheme: GlobalConfig,
): ConfigRepository {
    private val configDir: File
        get() = filePaths.baseConfigDir?.toFile() ?: throw IllegalStateException("Config directory not available")

    private val projectFile: File
        get() = File(configDir, filePaths.projectConfigFileName)

    init {
        // 設定ディレクトリが存在しない場合は作成
        ensureConfigDirExists()
    }

    private fun ensureConfigDirExists() {
        if (!configDir.exists()) {
            configDir.mkdirs()
        }
    }

    override fun loadGlobalConfig(): GlobalConfig {
        ensureConfigDirExists()
        return if (projectFile.exists()) {
            try {
                val yamlContent = projectFile.readText()
                Yaml.default.decodeFromString(GlobalConfigScheme.serializer(), yamlContent)
            } catch (e: Exception) {
                // ファイルの読み込みに失敗した場合はデフォルト設定を返す
                GlobalConfigScheme()
            }
        } else {
            // ファイルが存在しない場合はデフォルト設定を返す
            GlobalConfigScheme()
        }
    }

    override fun saveGlobalConfig(config: GlobalConfig) {
        ensureConfigDirExists()
        val yamlContent = Yaml.default.encodeToString(
            GlobalConfigScheme.serializer(), GlobalConfigScheme.from(config)
        )
        projectFile.writeText(yamlContent)
    }

    override fun getProjectConfigFilePath(): String {
        return projectFile.absolutePath
    }

    override fun loadKinfraConfig(filePath: String): KinfraConfig? {
        val file = resolveFilePath(filePath)
        if (!file.exists()) {
            return null
        }

        val yamlContent = file.readText()
        return Yaml.default.decodeFromString(KinfraConfigScheme.serializer(), yamlContent)
    }

    override fun saveKinfraConfig(config: KinfraConfig, filePath: String) {
        val file = resolveFilePath(filePath)
        val yamlContent = Yaml.default.encodeToString(KinfraConfigScheme.serializer(), KinfraConfigScheme.from(config))
        file.writeText(yamlContent)
    }

    override fun kinfraConfigExists(filePath: String): Boolean {
        return resolveFilePath(filePath).exists()
    }

    override fun loadKinfraParentConfig(filePath: String): KinfraParentConfig? {
        val file = resolveFilePath(filePath)
        if (!file.exists()) {
            return null
        }

        val yamlContent = file.readText()
        return Yaml.default.decodeFromString(KinfraParentConfigScheme.serializer(), yamlContent)
    }

    override fun saveKinfraParentConfig(config: KinfraParentConfig, filePath: String) {
        val file = resolveFilePath(filePath)
        val yamlContent = Yaml.default.encodeToString(
            KinfraParentConfigScheme.serializer(),
            KinfraParentConfigScheme.from(config)
        )
        file.writeText(yamlContent)
    }

    override fun kinfraParentConfigExists(filePath: String): Boolean {
        return resolveFilePath(filePath).exists()
    }

    private fun resolveFilePath(filePath: String): File {
        return File(filePath)
    }
}