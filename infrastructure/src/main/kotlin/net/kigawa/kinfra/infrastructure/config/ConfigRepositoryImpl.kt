package net.kigawa.kinfra.infrastructure.config

import com.charleskorn.kaml.Yaml
import net.kigawa.kinfra.model.ProjectConfig
import net.kigawa.kinfra.model.KinfraConfig
import net.kigawa.kinfra.model.FilePaths
import net.kigawa.kinfra.infrastructure.git.GitRepository
import java.io.File

class ConfigRepositoryImpl(
    private val baseConfigDir: String = FilePaths.BASE_CONFIG_DIR
) : ConfigRepository {

    /**
     * リポジトリ固有の設定ディレクトリを取得
     * リポジトリ名が取得できない場合は、baseConfigDirを返す（後方互換性）
     */
    private fun getRepoConfigDir(): String {
        val repoName = GitRepository.getRepositoryName()
        return if (repoName != null) {
            "$baseConfigDir/$repoName"
        } else {
            baseConfigDir
        }
    }

    private val configDir: String
        get() = getRepoConfigDir()

    private val projectConfigFile: File
        get() = File(configDir, FilePaths.PROJECT_CONFIG_FILE)

    init {
        // 設定ディレクトリが存在しない場合は作成
        ensureConfigDirExists()
    }

    private fun ensureConfigDirExists() {
        val dir = File(configDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    override fun loadProjectConfig(): ProjectConfig {
        ensureConfigDirExists()
        return if (projectConfigFile.exists()) {
            try {
                val yamlContent = projectConfigFile.readText()
                Yaml.default.decodeFromString(ProjectConfig.serializer(), yamlContent)
            } catch (e: Exception) {
                // ファイルの読み込みに失敗した場合はデフォルト設定を返す
                ProjectConfig()
            }
        } else {
            // ファイルが存在しない場合はデフォルト設定を返す
            ProjectConfig()
        }
    }

    override fun saveProjectConfig(config: ProjectConfig) {
        ensureConfigDirExists()
        val yamlContent = Yaml.default.encodeToString(ProjectConfig.serializer(), config)
        projectConfigFile.writeText(yamlContent)
    }

    override fun getProjectConfigFilePath(): String {
        return projectConfigFile.absolutePath
    }

    /**
     * ファイルパスを解決する
     * 相対パスの場合はログインしているリポジトリの設定ディレクトリを基準にする
     * 絶対パスの場合はそのまま返す
     */
    private fun resolveFilePath(filePath: String): File {
        return File(configDir, filePath)
    }

    override fun loadKinfraConfig(filePath: String): KinfraConfig? {
        val file = resolveFilePath(filePath)
        if (!file.exists()) {
            return null
        }

        val yamlContent = file.readText()
        return Yaml.default.decodeFromString(KinfraConfig.serializer(), yamlContent)
    }

    override fun saveKinfraConfig(config: KinfraConfig, filePath: String) {
        val file = resolveFilePath(filePath)
        val yamlContent = Yaml.default.encodeToString(KinfraConfig.serializer(), config)
        file.writeText(yamlContent)
    }

    override fun kinfraConfigExists(filePath: String): Boolean {
        return resolveFilePath(filePath).exists()
    }
}