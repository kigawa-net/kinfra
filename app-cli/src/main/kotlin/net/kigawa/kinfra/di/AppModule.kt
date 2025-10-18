package net.kigawa.kinfra.di

import net.kigawa.kinfra.TerraformRunner
import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.action.actions.AddSubProjectAction
import net.kigawa.kinfra.action.actions.ApplyAction
import net.kigawa.kinfra.action.actions.ConfigEditAction
import net.kigawa.kinfra.action.actions.DeployAction
import net.kigawa.kinfra.action.actions.DeployActionWithSDK
import net.kigawa.kinfra.action.actions.DestroyAction
import net.kigawa.kinfra.action.actions.FormatAction
import net.kigawa.kinfra.action.actions.HelloAction
import net.kigawa.kinfra.action.actions.HelpAction
import net.kigawa.kinfra.action.actions.InitAction
import net.kigawa.kinfra.actions.LoginAction
import net.kigawa.kinfra.action.actions.PlanAction
import net.kigawa.kinfra.action.actions.PushAction
import net.kigawa.kinfra.action.actions.SelfUpdateAction
import net.kigawa.kinfra.action.actions.SetupR2Action
import net.kigawa.kinfra.action.actions.SetupR2ActionWithSDK
import net.kigawa.kinfra.action.actions.StatusAction
import net.kigawa.kinfra.action.actions.ValidateAction
import net.kigawa.kinfra.action.bitwarden.BitwardenRepository
import net.kigawa.kinfra.action.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.git.GitHelperImpl
import net.kigawa.kinfra.infrastructure.bitwarden.BitwardenRepositoryImpl
import net.kigawa.kinfra.infrastructure.bitwarden.BitwardenSecretManagerRepositoryImpl
import net.kigawa.kinfra.infrastructure.config.ConfigRepositoryImpl
import net.kigawa.kinfra.infrastructure.config.LoginRepoImpl
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
import net.kigawa.kinfra.action.update.AutoUpdater
import net.kigawa.kinfra.infrastructure.update.AutoUpdaterImpl
import net.kigawa.kinfra.action.update.VersionChecker
import net.kigawa.kinfra.infrastructure.update.VersionCheckerImpl
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.ActionType
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.infrastructure.config.GlobalConfigScheme
import net.kigawa.kinfra.infrastructure.file.SystemHomeDirGetter
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.conf.GlobalConfig
import net.kigawa.kinfra.model.conf.HomeDirGetter
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
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
    single<VersionChecker> { VersionCheckerImpl(get()) }
    single<AutoUpdater> { AutoUpdaterImpl(get(), get()) }
    single<GitHelper> { GitHelperImpl(get()) }
    single<LoginRepo> { LoginRepoImpl(get(), get()) }

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
        println("⚠ BWS_ACCESS_TOKEN not available - SDK commands will not be registered")
    }

    if (hasBwsToken) {
        single<BitwardenSecretManagerRepository>(createdAtStart = true) {
            // .env から BW_PROJECT を読み込む
            val envFileLoader = EnvFileLoaderImpl()
            val projectId = envFileLoader.get("BW_PROJECT")
            BitwardenSecretManagerRepositoryImpl(bwsAccessToken, get(), projectId)
        }
    }

    // Presentation layer
    single<TerraformRunner> { TerraformRunner(get()) }

    // Actions
    single<Action>(named(ActionType.FMT.actionName)) { FormatAction(get(), get()) }
    single<Action>(named(ActionType.VALIDATE.actionName)) { ValidateAction(get(), get()) }
    single<Action>(named(ActionType.STATUS.actionName)) { StatusAction(get(), get()) }
    single<Action>(named(ActionType.LOGIN.actionName)) { LoginAction(get(), get(), get(), get(), get()) }
    single<Action>(named(ActionType.SETUP_R2.actionName)) { SetupR2Action(get(), get()) }
    single<Action>(named(ActionType.HELLO.actionName)) { HelloAction(get(), get(), get()) }
    single<Action>(named(ActionType.INIT.actionName)) { InitAction(get(), get()) }
    single<Action>(named(ActionType.PLAN.actionName)) { PlanAction(get(), get()) }
    single<Action>(named(ActionType.APPLY.actionName)) { ApplyAction(get()) }
    single<Action>(named(ActionType.DESTROY.actionName)) { DestroyAction(get(), get()) }
    single<Action>(named(ActionType.DEPLOY.actionName)) { DeployAction(get(), get()) }
    single<Action>(named(ActionType.PUSH.actionName)) { PushAction(get()) }
    single<Action>(named(ActionType.CONFIG_EDIT.actionName)) { ConfigEditAction(get(), get()) }
    single<Action>(named(ActionType.ADD_SUBPROJECT.actionName)) { AddSubProjectAction(get(), get()) }
    single<Action>(named(ActionType.SELF_UPDATE.actionName)) { SelfUpdateAction(get(), get(), get(), get(), get(), get()) }

    // SDK-based actions (only if BWS_ACCESS_TOKEN is available)
    if (hasBwsToken) {
        single<Action>(named(ActionType.SETUP_R2_SDK.actionName)) { SetupR2ActionWithSDK(get(), get(), get()) }
        single<Action>(named(ActionType.DEPLOY_SDK.actionName)) { DeployActionWithSDK(get(), get(), get(), get()) }
    }

    // Help action needs access to all actions
    single<Action>(named(ActionType.HELP.actionName)) {
        val actionMap = buildMap<String, Action> {
            ActionType.entries.forEach { actionType ->
                if (actionType != ActionType.HELP) {
                    runCatching {
                        put(actionType.actionName, get<Action>(named(actionType.actionName)))
                    }
                }
            }
        }

        HelpAction(actionMap, get())
    }
}