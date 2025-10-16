package net.kigawa.kinfra.infrastructure.config

import com.charleskorn.kaml.Yaml
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.conf.GlobalConfig
import net.kigawa.kinfra.model.conf.KinfraConfig
import net.kigawa.kinfra.model.conf.LoginConfig
import java.io.File
import java.nio.file.Path

class LoginRepoImpl(
    private val filePaths: FilePaths,
    private val globalConfig: GlobalConfig
) : LoginRepo {

    override val loginConfig: LoginConfig
        get() = globalConfig.login ?: throw IllegalStateException("Login config not available")

    override fun kinfraConfigPath(): Path {
        val repoDir = File("${filePaths.baseConfigDir}/${filePaths.reposDir}/${loginConfig.repo.substringAfterLast('/')}")
        val kinfraFile = File(repoDir, filePaths.kinfraConfigFileName)
        return kinfraFile.toPath()
    }

    override fun loadKinfraConfig(): KinfraConfig? {
        val file = kinfraConfigPath().toFile()
        if (!file.exists()) {
            return null
        }

        val yamlContent = file.readText()
        return Yaml.default.decodeFromString(KinfraConfigScheme.serializer(), yamlContent)
    }

    override fun saveKinfraConfig(config: KinfraConfig) {
        val file = kinfraConfigPath().toFile()
        file.parentFile?.mkdirs()
        val yamlContent = Yaml.default.encodeToString(KinfraConfigScheme.serializer(), KinfraConfigScheme.from(config))
        file.writeText(yamlContent)
    }

    override fun kinfraConfigExists(): Boolean {
        return kinfraConfigPath().toFile().exists()
    }
}
