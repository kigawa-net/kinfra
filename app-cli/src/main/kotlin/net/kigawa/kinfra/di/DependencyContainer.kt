package net.kigawa.kinfra.di

import net.kigawa.kinfra.TerraformRunner
import net.kigawa.kinfra.action.actions.*
import net.kigawa.kinfra.action.bitwarden.BitwardenRepository
import net.kigawa.kinfra.action.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.action.config.EnvFileLoader
import net.kigawa.kinfra.action.execution.ActionExecutor
import net.kigawa.kinfra.action.execution.SubProjectExecutor
import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.action.update.AutoUpdater
import net.kigawa.kinfra.action.update.VersionChecker
import net.kigawa.kinfra.actions.LoginAction
import net.kigawa.kinfra.git.GitHelperImpl
import net.kigawa.kinfra.infrastructure.bitwarden.BitwardenRepositoryImpl
import net.kigawa.kinfra.infrastructure.bitwarden.BitwardenSecretManagerRepositoryImpl
import net.kigawa.kinfra.infrastructure.config.ConfigRepositoryImpl
import net.kigawa.kinfra.infrastructure.config.GlobalConfigCompleterImpl
import net.kigawa.kinfra.model.conf.GlobalConfigCompleter
import net.kigawa.kinfra.infrastructure.config.EnvFileLoaderImpl
import net.kigawa.kinfra.infrastructure.config.LoginRepoImpl
import net.kigawa.kinfra.infrastructure.file.FileRepositoryImpl
import net.kigawa.kinfra.infrastructure.file.SystemHomeDirGetter
import net.kigawa.kinfra.infrastructure.logging.FileLogger
import net.kigawa.kinfra.infrastructure.logging.LogLevel
import net.kigawa.kinfra.infrastructure.process.ProcessExecutorImpl
import net.kigawa.kinfra.infrastructure.service.TerraformServiceImpl
import net.kigawa.kinfra.infrastructure.terraform.TerraformRepositoryImpl
import net.kigawa.kinfra.infrastructure.update.AutoUpdaterImpl
import net.kigawa.kinfra.infrastructure.update.VersionCheckerImpl
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.ActionType
import net.kigawa.kinfra.model.GitHelper
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.SubActionType
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.conf.global.GlobalConfig
import net.kigawa.kinfra.model.conf.HomeDirGetter
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.service.ActionRegistry
import net.kigawa.kinfra.service.CommandInterpreter
import net.kigawa.kinfra.service.SystemRequirement
import net.kigawa.kinfra.service.UpdateHandler

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

    val envFileLoader: EnvFileLoader by lazy { EnvFileLoaderImpl() }
    val fileRepository by lazy { FileRepositoryImpl() }
    val processExecutor by lazy { ProcessExecutorImpl() }
    val globalConfigCompleter: GlobalConfigCompleter by lazy { GlobalConfigCompleterImpl(filePaths) }
    val configRepository: ConfigRepository by lazy { ConfigRepositoryImpl(filePaths, logger, globalConfigCompleter) }
    val terraformRepository by lazy { TerraformRepositoryImpl(fileRepository, configRepository) }
    val terraformService: TerraformService by lazy { TerraformServiceImpl(processExecutor, terraformRepository) }
    val bitwardenRepository: BitwardenRepository by lazy { BitwardenRepositoryImpl(processExecutor, filePaths) }

    val globalConfig: GlobalConfig by lazy {
        configRepository.loadGlobalConfig()
    }

    val versionChecker: VersionChecker by lazy { VersionCheckerImpl(logger) }
    val autoUpdater: AutoUpdater by lazy { AutoUpdaterImpl(logger, filePaths) }
    val gitHelper: GitHelper by lazy { GitHelperImpl(configRepository) }
    val loginRepo: LoginRepo by lazy { LoginRepoImpl(filePaths, globalConfig) }

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
            println("⚠ BWS_ACCESS_TOKEN not available - SDK commands will not be registered")
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

    // Service layer
    val actionRegistry: ActionRegistry by lazy { ActionRegistry(this) }
    val commandInterpreter: CommandInterpreter by lazy { CommandInterpreter(logger) }
    val systemRequirement: SystemRequirement by lazy { SystemRequirement(logger) }
    val updateHandler: UpdateHandler by lazy { UpdateHandler(versionChecker, autoUpdater, logger, configRepository, loginRepo) }

    // Execution layer
    val actionExecutor: ActionExecutor by lazy { ActionExecutor(logger) }
    val subProjectExecutor: SubProjectExecutor by lazy { SubProjectExecutor(configRepository, loginRepo) }

    // Presentation layer
    val terraformRunner: TerraformRunner by lazy { TerraformRunner(this) }

    // Actions (without HelpAction first to avoid circular dependency)
    private val actionsWithoutHelp: Map<Pair<String, SubActionType?>, Action> by lazy {
        buildMap {
            // Regular actions
            put(Pair(ActionType.FMT.actionName, null), FormatAction(terraformService, gitHelper))
            put(Pair(ActionType.VALIDATE.actionName, null), ValidateAction(terraformService, gitHelper))
            put(Pair(ActionType.STATUS.actionName, null), StatusAction(terraformService, gitHelper))
            put(Pair(ActionType.LOGIN.actionName, null), LoginAction(
                bitwardenRepository,
                configRepository,
                gitHelper,
                filePaths,
                loginRepo
            ))
            put(Pair(ActionType.HELLO.actionName, null), HelloAction(terraformService, logger, gitHelper))
            put(Pair(ActionType.INIT.actionName, null), InitAction(terraformService, gitHelper))
            put(Pair(ActionType.PLAN.actionName, null), PlanAction(terraformService, gitHelper, subProjectExecutor))
            put(Pair(ActionType.APPLY.actionName, null), ApplyAction(terraformService))
            put(Pair(ActionType.DESTROY.actionName, null), DestroyAction(terraformService, gitHelper))
            put(Pair(ActionType.DEPLOY.actionName, null), DeployAction(
                terraformService,
                bitwardenRepository,
                configRepository,
                loginRepo,
                logger
            ))
            put(Pair(ActionType.PUSH.actionName, null), PushAction(gitHelper))
            put(Pair(ActionType.CONFIG.actionName, null), ConfigAction(loginRepo))
            put(Pair(ActionType.CONFIG_EDIT.actionName, null), ConfigEditAction(loginRepo, logger))
            put(Pair(ActionType.SELF_UPDATE.actionName, null), SelfUpdateAction(
                versionChecker,
                autoUpdater,
                gitHelper,
                loginRepo,
                logger
            ))

            // Subcommands
            put(Pair(ActionType.SUB.actionName, SubActionType.LIST), SubListAction(loginRepo))
            put(Pair(ActionType.SUB.actionName, SubActionType.ADD), SubAddAction(loginRepo))
            put(Pair(ActionType.SUB.actionName, SubActionType.SHOW), SubShowAction(
                configRepository,
                filePaths,
                loginRepo
            ))
            put(Pair(ActionType.SUB.actionName, SubActionType.EDIT), SubEditAction(
                loginRepo,
                logger
            ))
            put(Pair(ActionType.SUB.actionName, SubActionType.REMOVE), SubRemoveAction(loginRepo))
            put(Pair(ActionType.SUB.actionName, SubActionType.PLAN), SubPlanAction(
                loginRepo,
                subProjectExecutor
            ))

            // SDK-based actions (only if BWS_ACCESS_TOKEN is available)
            if (hasBwsToken && bitwardenSecretManagerRepository != null) {
                put(Pair(ActionType.DEPLOY_SDK.actionName, null), DeployActionWithSDK(
                    terraformService,
                    bitwardenSecretManagerRepository!!,
                    configRepository,
                    loginRepo,
                    logger,
                    envFileLoader
                ))
            }
        }
    }

    // All actions including HelpAction
    private val actions: Map<Pair<String, SubActionType?>, Action> by lazy {
        actionsWithoutHelp.toMutableMap().apply {
            // Help action needs access to all actions (without help itself)
            val actionsForHelp = buildMap {
                ActionType.entries.forEach { actionType ->
                    if (actionType == ActionType.SUB) {
                        SubActionType.entries.forEach { subActionType ->
                            actionsWithoutHelp[Pair(actionType.actionName, subActionType)]?.let {
                                put("${actionType.actionName} ${subActionType.actionName}", it)
                            }
                        }
                    } else if (actionType != ActionType.HELP) {
                        actionsWithoutHelp[Pair(actionType.actionName, null)]?.let {
                            put(actionType.actionName, it)
                        }
                    }
                }
            }
            put(Pair(ActionType.HELP.actionName, null), HelpAction(actionsForHelp, gitHelper))
        }
    }

    fun getAction(actionName: String, subActionType: SubActionType? = null): Action? {
        return actions[Pair(actionName, subActionType)]
    }

    fun getAllActions(): Map<String, Action> {
        return buildMap {
            ActionType.entries.forEach { actionType ->
                if (actionType == ActionType.SUB) {
                    SubActionType.entries.forEach { subActionType ->
                        actions[Pair(actionType.actionName, subActionType)]?.let {
                            put("${actionType.actionName} ${subActionType.actionName}", it)
                        }
                    }
                } else if (actionType != ActionType.HELP) {
                    actions[Pair(actionType.actionName, null)]?.let {
                        put(actionType.actionName, it)
                    }
                }
            }
        }
    }
}
