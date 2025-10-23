package net.kigawa.kinfra.infrastructure.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.conf.GlobalConfigCompleter
import net.kigawa.kinfra.model.conf.global.GlobalConfig
import net.kigawa.kinfra.model.conf.KinfraConfig
import net.kigawa.kinfra.model.conf.KinfraParentConfig
import net.kigawa.kinfra.infrastructure.logging.Logger

import java.io.File
import java.nio.file.Path

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
        return if (file.exists()) yaml.decodeFromString(KinfraConfigScheme.serializer(), file.readText()) else null
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
