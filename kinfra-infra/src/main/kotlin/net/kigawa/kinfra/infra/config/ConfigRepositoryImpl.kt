package net.kigawa.kinfra.infra.config

import com.charleskorn.kaml.Yaml
import net.kigawa.kinfra.infra.config.script.KinfraConfigKtsWriter
import net.kigawa.kinfra.infra.config.script.KinfraConfigScript
import net.kigawa.kinfra.infra.config.script.KinfraParentConfigKtsWriter
import net.kigawa.kinfra.infra.config.script.ScriptConfigCache
import net.kigawa.kinfra.infra.config.script.ScriptHost
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.conf.GlobalConfigCompleter
import net.kigawa.kinfra.model.conf.KinfraConfig
import net.kigawa.kinfra.model.conf.KinfraParentConfig
import net.kigawa.kinfra.model.conf.global.GlobalConfig
import net.kigawa.kinfra.model.config.ConfigRepository
import net.kigawa.kodel.api.log.traceignore.debug
import java.io.File
import java.nio.file.Path

/**
 * 設定ファイルを操作する実装です。
 */
class ConfigRepositoryImpl(
    private val filePaths: FilePaths,
    private val kogger: net.kigawa.kodel.api.log.Kogger,
    private val globalConfigCompleter: GlobalConfigCompleter,
): ConfigRepository {
    // 基本設定ディレクトリ
    private val configDir
        get() =
            filePaths.baseConfigDir?.toFile()
                ?: throw IllegalStateException("Config directory not available")

    // project.yaml の場所
    private val projectFile get() = File(configDir, filePaths.projectConfigFileName)

    init {
        ensureConfigDirExists()
    }

    private fun ensureConfigDirExists() {
        if (!configDir.exists()) configDir.mkdirs()
    }

    override fun loadGlobalConfig(): GlobalConfig {
        return if (projectFile.exists()) {
            try {
                val yaml = projectFile.readText()
                val scheme = Yaml.default.decodeFromString(GlobalConfigScheme.serializer(), yaml)

                // GlobalConfigCompleter を使って設定を補完
                val completedConfig = globalConfigCompleter.complete(scheme)

                // 設定が変更された場合は保存
                if (completedConfig is GlobalConfigImpl && completedConfig.globalConfigScheme != scheme) {
                    saveGlobalConfig(completedConfig)
                    kogger.info("設定ファイルを更新しました")
                }

                completedConfig
            } catch (e: Exception) {
                kogger.debug("設定ファイルの読み込みに失敗: ${e.message}")
                val reposPath =
                    filePaths.baseConfigDir?.resolve(filePaths.reposDir)
                        ?: throw IllegalStateException("Config directory not available")
                GlobalConfigImpl(GlobalConfigScheme(), reposPath)
            }
        } else {
            val reposPath =
                filePaths.baseConfigDir?.resolve(filePaths.reposDir)
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
        return ScriptConfigCache.loadOrEval(file, KinfraConfigScheme.serializer()) {
            ScriptHost.eval<KinfraConfigScript>(file).toScheme()
        }
    }

    override fun saveKinfraConfig(
        config: KinfraConfig,
        filePath: String,
    ) {
        val file = File(filePath)
        file.writeText(KinfraConfigKtsWriter.render(KinfraConfigScheme.from(config)))
    }

    override fun kinfraConfigExists(filePath: String) = File(filePath).exists()

    override fun saveKinfraParentConfig(
        config: KinfraParentConfig,
        filePath: String,
    ) {
        val file = File(filePath)
        file.writeText(KinfraParentConfigKtsWriter.render(KinfraParentConfigScheme.from(config)))
    }

    override fun loadKinfraParentConfig(filePath: String): KinfraParentConfig? {
        val file = File(filePath)
        return if (file.exists()) {
            KinfraParentConfigImpl.fromFile(file)
        } else {
            null
        }
    }

    override fun kinfraParentConfigExists(filePath: String) = File(filePath).exists()
}
