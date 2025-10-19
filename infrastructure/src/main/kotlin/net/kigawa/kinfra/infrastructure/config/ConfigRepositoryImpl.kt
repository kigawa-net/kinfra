package net.kigawa.kinfra.infrastructure.config

import com.charleskorn.kaml.Yaml
import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.conf.GlobalConfig
import net.kigawa.kinfra.model.conf.KinfraConfig
import net.kigawa.kinfra.model.conf.KinfraParentConfig
import net.kigawa.kinfra.infrastructure.config.GlobalConfigImpl
import net.kigawa.kinfra.infrastructure.config.KinfraParentConfigImpl

import java.io.File
import java.nio.file.Path

/**
 * 設定ファイルを操作する実装です。
 */
class ConfigRepositoryImpl(
    private val filePaths: FilePaths,
) : ConfigRepository {
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
                GlobalConfigImpl(scheme, reposPath)
            } catch (e: Exception) {
                GlobalConfigImpl(GlobalConfigScheme(), reposPath)
            }
        } else {
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
        return if (file.exists()) Yaml.default.decodeFromString(KinfraConfigScheme.serializer(), file.readText()) else null
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
