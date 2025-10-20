package net.kigawa.kinfra.infrastructure.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.conf.GlobalConfig
import net.kigawa.kinfra.model.conf.KinfraConfig
import net.kigawa.kinfra.model.conf.KinfraParentConfig
import net.kigawa.kinfra.model.conf.LoginConfig
import net.kigawa.kinfra.infrastructure.config.GlobalConfigImpl
import net.kigawa.kinfra.infrastructure.config.KinfraParentConfigImpl
import net.kigawa.kinfra.infrastructure.logging.Logger

import java.io.File
import java.nio.file.Path

/**
 * 設定ファイルを操作する実装です。
 */
class ConfigRepositoryImpl(
    private val filePaths: FilePaths,
    private val logger: Logger,
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
                
                // repoPathが不足している場合に対話形式で補完
                val completedScheme = completeMissingLoginConfig(scheme)
                
                // 設定が変更された場合は保存
                if (completedScheme != scheme) {
                    saveGlobalConfig(GlobalConfigImpl(completedScheme, reposPath))
                    logger.info("設定ファイルを更新しました")
                }
                
                GlobalConfigImpl(completedScheme, reposPath)
            } catch (e: Exception) {
                logger.debug("設定ファイルの読み込みに失敗: ${e.message}")
                GlobalConfigImpl(GlobalConfigScheme(), reposPath)
            }
        } else {
            GlobalConfigImpl(GlobalConfigScheme(), reposPath)
        }
    }
    
    /**
     * 不足しているLoginConfigの項目を対話形式で補完する
     */
    private fun completeMissingLoginConfig(scheme: GlobalConfigScheme): GlobalConfigScheme {
        val login = scheme.login ?: return scheme

        var modified = false
        var repo = login.repo
        var repoPath = login.repoPath
        var enabledProjects = login.enabledProjects

        // repoが不足している場合
        if (repo.isBlank()) {
            print("リポジトリURLを入力してください (例: https://github.com/user/repo.git): ")
            repo = readlnOrNull()?.trim() ?: ""
            modified = true
        }

        // repoPathが不足している場合
        if (repoPath.toString().isBlank()) {
            val defaultPath = filePaths.baseConfigDir?.resolve("repos") ?: Path.of("./repos")
            print("リポジトリのローカルパスを入力してください (デフォルト: $defaultPath): ")
            val input = readlnOrNull()?.trim() ?: ""
            repoPath = if (input.isBlank()) defaultPath.toString() else input
            modified = true
        }

        // enabledProjectsが空の場合
        if (enabledProjects.isEmpty()) {
            print("有効なプロジェクト名をカンマ区切りで入力してください (例: project1,project2): ")
            val input = readlnOrNull()?.trim() ?: ""
            if (input.isNotBlank()) {
                enabledProjects = input.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                modified = true
            }
        }

        return if (modified) {
            val completedLogin = LoginConfigScheme(
                repo = repo,
                repoPath = repoPath,
                enabledProjects = enabledProjects
            )
            scheme.copy(login = completedLogin)
        } else {
            scheme
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
