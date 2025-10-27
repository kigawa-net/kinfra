package net.kigawa.kinfra.di

import net.kigawa.kinfra.model.bitwarden.BitwardenRepository
import net.kigawa.kinfra.model.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.model.config.ConfigRepository
import net.kigawa.kinfra.model.config.EnvFileLoader
import net.kigawa.kinfra.infrastructure.bitwarden.BitwardenRepositoryImpl
import net.kigawa.kinfra.infrastructure.bitwarden.BitwardenSecretManagerRepositoryImpl
import net.kigawa.kinfra.infrastructure.config.ConfigRepositoryImpl
import net.kigawa.kinfra.infrastructure.config.EnvFileLoaderImpl
import net.kigawa.kinfra.infrastructure.config.GlobalConfigCompleterImpl
import net.kigawa.kinfra.model.conf.GlobalConfigCompleter
import net.kigawa.kinfra.infrastructure.file.FileRepository
import net.kigawa.kinfra.infrastructure.file.FileRepositoryImpl
import net.kigawa.kinfra.infrastructure.file.SystemHomeDirGetter
import net.kigawa.kinfra.infrastructure.logging.FileLogger
import net.kigawa.kinfra.infrastructure.logging.LogLevel
import net.kigawa.kinfra.infrastructure.logging.Logger
import net.kigawa.kinfra.infrastructure.process.ProcessExecutor
import net.kigawa.kinfra.infrastructure.process.ProcessExecutorImpl
import net.kigawa.kinfra.infrastructure.service.TerraformServiceImpl
import net.kigawa.kinfra.infrastructure.terraform.TerraformRepository
import net.kigawa.kinfra.infrastructure.terraform.TerraformRepositoryImpl
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.conf.global.GlobalConfig
import net.kigawa.kinfra.model.conf.HomeDirGetter
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.infrastructure.config.LoginRepoImpl
import net.kigawa.kinfra.model.service.TerraformService

class DependencyContainer {
    // Infrastructure layer
    val homeDirGetter: HomeDirGetter by lazy { SystemHomeDirGetter() }
    val filePaths: FilePaths by lazy { FilePaths(homeDirGetter) }



    val logger: Logger by lazy {
        val logDir = System.getenv("KINFRA_LOG_DIR") ?: "logs"
        val logLevelStr = System.getenv("KINFRA_LOG_LEVEL") ?: "INFO"
        val logLevel = try {
            LogLevel.valueOf(logLevelStr.uppercase())
        } catch (e: IllegalArgumentException) {
            LogLevel.INFO
        }
        FileLogger(logDir, logLevel)
    }

    val globalConfigCompleter: GlobalConfigCompleter by lazy { GlobalConfigCompleterImpl(filePaths) }
    val envFileLoader: EnvFileLoader by lazy { EnvFileLoaderImpl() }
    val fileRepository: FileRepository by lazy { FileRepositoryImpl() }
    val processExecutor: ProcessExecutor by lazy { ProcessExecutorImpl() }
    val configRepository: ConfigRepository by lazy { ConfigRepositoryImpl(filePaths, logger, globalConfigCompleter) }

    val globalConfig: GlobalConfig by lazy {
        configRepository.loadGlobalConfig()
    }

    val loginRepo: LoginRepo by lazy { LoginRepoImpl(filePaths, globalConfig) }

    val terraformRepository: TerraformRepository by lazy { TerraformRepositoryImpl(fileRepository, configRepository, loginRepo) }
    val terraformService: TerraformService by lazy { TerraformServiceImpl(processExecutor, terraformRepository, configRepository, bitwardenSecretManagerRepository) }
    val bitwardenRepository: BitwardenRepository by lazy { BitwardenRepositoryImpl(processExecutor, filePaths) }

    // Bitwarden Secret Manager
    private val bwsAccessToken: String? by lazy {
        System.getenv("BWS_ACCESS_TOKEN")?.also {
            println("✓ Using BWS_ACCESS_TOKEN from environment variable")
        } ?: run {
            val tokenFile = filePaths.bwsTokenFile?.toFile()
            if (tokenFile != null && tokenFile.exists() && tokenFile.canRead()) {
                tokenFile.readText().trim().takeIf { it.isNotBlank() }?.also {
                    println("✓ Loaded BWS_ACCESS_TOKEN from .bws_token file")
                }
            } else {
                null
            }
        }
    }

    val hasBwsToken: Boolean by lazy {
        val hasToken = bwsAccessToken != null && bwsAccessToken!!.isNotBlank()
        if (!hasToken) {
            println("⚠ BWS_ACCESS_TOKEN not available - Secret Manager features will be limited")
        }
        hasToken
    }

    val bitwardenSecretManagerRepository: BitwardenSecretManagerRepository? by lazy {
        if (hasBwsToken) {
            val projectId = envFileLoader.get("BW_PROJECT")
            BitwardenSecretManagerRepositoryImpl(bwsAccessToken!!, processExecutor, projectId)
        } else {
            null
        }
    }
}
