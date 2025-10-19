package net.kigawa.kinfra.infrastructure.config

import com.charleskorn.kaml.Yaml
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.conf.*
import java.io.File
import java.nio.file.Path

class LoginRepoImpl(
    private val filePaths: FilePaths,
    private val globalConfig: GlobalConfig,
): LoginRepo {

    override val loginConfig: LoginConfig
        get() = globalConfig.login ?: throw IllegalStateException("Login config not available")
    val repoDir = File(
        "${filePaths.baseConfigDir}/${filePaths.reposDir}/" +
            loginConfig.repo.substringAfterLast('/')
    ).toPath()

    override fun kinfraConfigPath(): Path {
        return repoDir.resolve(filePaths.kinfraConfigFileName)

    }

    override fun kinfraParentConfigPath(): Path {
        return repoDir.resolve(filePaths.kinfraParentConfigFileName)
    }

    val kinfraParentConfigFile: File = kinfraParentConfigPath().toFile()
    override fun loadKinfraParentConfig(): KinfraParentConfigImpl? {
        if (!kinfraParentConfigFile.exists()) {
            return null
        }
        return KinfraParentConfigImpl.fromFile(kinfraParentConfigFile)
    }

    override fun createKinfraParentConfig(
        kinfraParentConfigData: KinfraParentConfigData,
    ): KinfraParentConfig {
        kinfraParentConfigFile.parentFile.mkdirs()
        val scheme = KinfraParentConfigScheme.from(kinfraParentConfigData)
        kinfraParentConfigFile.writeText(
            Yaml.default.encodeToString(
                KinfraParentConfigScheme.serializer(), scheme
            )
        )
        return KinfraParentConfigImpl(scheme, kinfraParentConfigFile)
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
