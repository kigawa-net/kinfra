package net.kigawa.kinfra.infrastructure.config

import com.charleskorn.kaml.Yaml
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.kigawa.kinfra.model.HostsConfig
import net.kigawa.kinfra.model.ProjectConfig
import net.kigawa.kinfra.model.KinfraConfig
import net.kigawa.kinfra.infrastructure.git.GitRepository
import java.io.File

class ConfigRepositoryImpl(
    private val baseConfigDir: String = System.getProperty("user.home") + "/.local/kinfra"
) : ConfigRepository {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

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

    private val configFile: File
        get() = File(configDir, "hosts.json")

    private val projectConfigFile: File
        get() = File(configDir, "project.json")

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

    override fun loadHostsConfig(): HostsConfig {
        ensureConfigDirExists()
        return if (configFile.exists()) {
            try {
                val json = configFile.readText()
                gson.fromJson(json, HostsConfig::class.java)
            } catch (e: Exception) {
                // ファイルの読み込みに失敗した場合はデフォルト設定を返す
                HostsConfig(HostsConfig.DEFAULT_HOSTS)
            }
        } else {
            // ファイルが存在しない場合はデフォルト設定を返す
            HostsConfig(HostsConfig.DEFAULT_HOSTS)
        }
    }

    override fun saveHostsConfig(config: HostsConfig) {
        ensureConfigDirExists()
        val json = gson.toJson(config)
        configFile.writeText(json)
    }

    override fun getConfigFilePath(): String {
        return configFile.absolutePath
    }

    override fun loadProjectConfig(): ProjectConfig {
        ensureConfigDirExists()
        return if (projectConfigFile.exists()) {
            try {
                val json = projectConfigFile.readText()
                gson.fromJson(json, ProjectConfig::class.java)
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
        val json = gson.toJson(config)
        projectConfigFile.writeText(json)
    }

    override fun getProjectConfigFilePath(): String {
        return projectConfigFile.absolutePath
    }

    /**
     * ファイルパスを解決する
     * 相対パスの場合はGitリポジトリのルートを基準にする
     * 絶対パスの場合はそのまま返す
     */
    private fun resolveFilePath(filePath: String): File {
        val file = File(filePath)
        if (file.isAbsolute) {
            return file
        }

        // 相対パスの場合、Gitリポジトリのルートを基準にする
        val repoRoot = GitRepository.getRepositoryRoot()
        return if (repoRoot != null) {
            File(repoRoot, filePath)
        } else {
            // Gitリポジトリでない場合は、カレントディレクトリを基準にする
            file
        }
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