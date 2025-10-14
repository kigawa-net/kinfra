package net.kigawa.kinfra.di

import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.action.bitwarden.BitwardenRepository
import net.kigawa.kinfra.action.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.infrastructure.bitwarden.BitwardenRepositoryImpl
import net.kigawa.kinfra.infrastructure.bitwarden.BitwardenSecretManagerRepositoryImpl
import net.kigawa.kinfra.infrastructure.config.ConfigRepositoryImpl
import net.kigawa.kinfra.action.config.EnvFileLoader
import net.kigawa.kinfra.infrastructure.config.EnvFileLoaderImpl
import net.kigawa.kinfra.infrastructure.file.FileRepository
import net.kigawa.kinfra.infrastructure.file.FileRepositoryImpl
import net.kigawa.kinfra.infrastructure.logging.FileLogger
import net.kigawa.kinfra.infrastructure.logging.LogLevel
import net.kigawa.kinfra.infrastructure.logging.Logger
import net.kigawa.kinfra.infrastructure.process.ProcessExecutor
import net.kigawa.kinfra.infrastructure.process.ProcessExecutorImpl
import net.kigawa.kinfra.infrastructure.service.TerraformServiceImpl
import net.kigawa.kinfra.infrastructure.terraform.TerraformRepository
import net.kigawa.kinfra.infrastructure.terraform.TerraformRepositoryImpl
import net.kigawa.kinfra.infrastructure.config.GlobalConfigScheme
import net.kigawa.kinfra.infrastructure.file.SystemHomeDirGetter
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.conf.GlobalConfig
import net.kigawa.kinfra.model.conf.HomeDirGetter
import org.koin.dsl.module

val webModule = module {
    // Infrastructure layer
    single<HomeDirGetter> { SystemHomeDirGetter() }
    single<FilePaths> { FilePaths(get()) }
    // GlobalConfig: Load from file, or use default empty config
    single<GlobalConfig> {
        val configRepo = ConfigRepositoryImpl(get(), GlobalConfigScheme())
        runCatching { configRepo.loadGlobalConfig() }.getOrElse { GlobalConfigScheme() }
    }
    single<Logger> {
        val logDir = System.getenv("KINFRA_LOG_DIR") ?: "logs"
        val logLevelStr = System.getenv("KINFRA_LOG_LEVEL") ?: "INFO"
        val logLevel = try {
            LogLevel.valueOf(logLevelStr.uppercase())
        } catch (e: IllegalArgumentException) {
            LogLevel.INFO
        }
        FileLogger(logDir, logLevel)
    }
    single<EnvFileLoader> { EnvFileLoaderImpl() }
    single<FileRepository> { FileRepositoryImpl() }
    single<ProcessExecutor> { ProcessExecutorImpl() }
    single<TerraformRepository> { TerraformRepositoryImpl(get()) }
    single<TerraformService> { TerraformServiceImpl(get(), get()) }
    single<BitwardenRepository> { BitwardenRepositoryImpl(get(),get()) }
    single<ConfigRepository> { ConfigRepositoryImpl(get(),get()) }

    // Bitwarden Secret Manager (環境変数または .bws_token ファイルから BWS_ACCESS_TOKEN を取得)
    // Note: FilePaths は既に登録されているので、早期に取得可能
    val filePathsInstance = FilePaths(SystemHomeDirGetter())
    val bwsAccessToken = System.getenv("BWS_ACCESS_TOKEN")?.also {
        println("✓ Using BWS_ACCESS_TOKEN from environment variable")
    } ?: run {
        // ファイルから読み込み
        val tokenFile = java.io.File(filePathsInstance.bwsTokenFileName)
        if (tokenFile.exists() && tokenFile.canRead()) {
            tokenFile.readText().trim().takeIf { it.isNotBlank() }?.also {
                println("✓ Loaded BWS_ACCESS_TOKEN from .bws_token file")
            }
        } else {
            null
        }
    }
    val hasBwsToken = bwsAccessToken != null && bwsAccessToken.isNotBlank()

    if (!hasBwsToken) {
        println("⚠ BWS_ACCESS_TOKEN not available - Secret Manager features will be limited")
    }

    if (hasBwsToken) {
        single<BitwardenSecretManagerRepository>(createdAtStart = true) {
            // .env から BW_PROJECT を読み込む
            val envFileLoader = EnvFileLoaderImpl()
            val projectId = envFileLoader.get("BW_PROJECT")
            BitwardenSecretManagerRepositoryImpl(bwsAccessToken!!, get(), projectId)
        }
    }
}