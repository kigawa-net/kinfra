package net.kigawa.kinfra.di

import net.kigawa.kinfra.model.GitHelper
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.action.update.AutoUpdater
import net.kigawa.kinfra.action.update.VersionChecker
import net.kigawa.kinfra.git.GitHelperImpl
import net.kigawa.kinfra.infrastructure.bitwarden.BitwardenRepositoryImpl
import net.kigawa.kinfra.infrastructure.config.ConfigRepositoryImpl
import net.kigawa.kinfra.infrastructure.config.EnvFileLoaderImpl
import net.kigawa.kinfra.infrastructure.config.GlobalConfigScheme
import net.kigawa.kinfra.infrastructure.config.GlobalConfigImpl
import net.kigawa.kinfra.infrastructure.config.LoginRepoImpl
import net.kigawa.kinfra.infrastructure.file.FileRepositoryImpl
import net.kigawa.kinfra.infrastructure.logging.FileLogger
import net.kigawa.kinfra.infrastructure.logging.LogLevel
import net.kigawa.kinfra.infrastructure.logging.Logger
import net.kigawa.kinfra.infrastructure.process.ProcessExecutorImpl
import net.kigawa.kinfra.infrastructure.service.TerraformServiceImpl
import net.kigawa.kinfra.infrastructure.terraform.TerraformRepositoryImpl
import net.kigawa.kinfra.infrastructure.update.AutoUpdaterImpl
import net.kigawa.kinfra.infrastructure.update.VersionCheckerImpl
import net.kigawa.kinfra.action.bitwarden.BitwardenRepository
import net.kigawa.kinfra.action.config.EnvFileLoader
import net.kigawa.kinfra.infrastructure.file.FileRepository
import net.kigawa.kinfra.infrastructure.file.SystemHomeDirGetter
import net.kigawa.kinfra.infrastructure.process.ProcessExecutor
import net.kigawa.kinfra.infrastructure.terraform.TerraformRepository
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.conf.GlobalConfig
import net.kigawa.kinfra.model.conf.HomeDirGetter
import org.koin.dsl.module

val infrastructureModule = module {
    // Infrastructure layer
    single<HomeDirGetter> { SystemHomeDirGetter() }
    single<FilePaths> { FilePaths(get()) }
    // GlobalConfig: Load from file, or use default empty config
    single<GlobalConfig> {
        val configRepo = ConfigRepositoryImpl(get(), get())
        configRepo.loadGlobalConfig()
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
    single<TerraformRepository> { TerraformRepositoryImpl(get(), get()) }
    single<TerraformService> { TerraformServiceImpl(get(), get()) }
    single<BitwardenRepository> { BitwardenRepositoryImpl(get(), get()) }
    single<ConfigRepository> { ConfigRepositoryImpl(get(), get()) }
    single<VersionChecker> { VersionCheckerImpl(get()) }
    single<AutoUpdater> { AutoUpdaterImpl(get(), get()) }
    single<GitHelper> { GitHelperImpl(get()) }
    single<LoginRepo> { LoginRepoImpl(get(), get()) }
}