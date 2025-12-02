package net.kigawa.kinfra.di

import net.kigawa.kinfra.action.actions.*
import net.kigawa.kinfra.actions.LoginAction
import net.kigawa.kinfra.git.GitHelperImpl
import net.kigawa.kinfra.infra.action.actions.NextAction
import net.kigawa.kinfra.infra.action.actions.SubmoduleAction
import net.kigawa.kinfra.infra.bitwarden.BitwardenRepositoryImpl
import net.kigawa.kinfra.infra.bitwarden.BitwardenSecretManagerRepositoryImpl
import net.kigawa.kinfra.infra.config.ConfigRepositoryImpl
import net.kigawa.kinfra.infra.config.EnvFileLoaderImpl
import net.kigawa.kinfra.infra.config.GlobalConfigCompleterImpl
import net.kigawa.kinfra.infra.config.LoginRepoImpl
import net.kigawa.kinfra.infra.file.FileRepositoryImpl
import net.kigawa.kinfra.infra.file.SystemHomeDirGetter
import net.kigawa.kinfra.infra.process.ProcessExecutorImpl
import net.kigawa.kinfra.infra.terraform.TerraformRepositoryImpl
import net.kigawa.kinfra.infra.update.AutoUpdaterImpl
import net.kigawa.kinfra.infra.update.VersionCheckerImpl
import net.kigawa.kinfra.model.*
import net.kigawa.kinfra.model.bitwarden.BitwardenRepository
import net.kigawa.kinfra.model.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.conf.GlobalConfigCompleter
import net.kigawa.kinfra.model.conf.HomeDirGetter
import net.kigawa.kinfra.model.conf.global.GlobalConfig
import net.kigawa.kinfra.model.config.ConfigRepository
import net.kigawa.kinfra.model.config.EnvFileLoader
import net.kigawa.kinfra.model.execution.SubProjectExecutor
import net.kigawa.kodel.api.log.LogLevel
import net.kigawa.kodel.api.log.Kogger
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.model.update.AutoUpdater
import net.kigawa.kinfra.model.update.VersionChecker
import net.kigawa.kinfra.service.CommandInterpreter
import net.kigawa.kinfra.service.SystemRequirement
import net.kigawa.kinfra.service.UpdateHandler
import net.kigawa.kodel.api.log.LoggerFactory
import net.kigawa.kodel.api.log.getLogger

class DependencyContainer {
    // Infrastructure layer
    val homeDirGetter: HomeDirGetter by lazy { SystemHomeDirGetter() }
    val filePaths: FilePaths by lazy { FilePaths(homeDirGetter) }

    val kogger: Kogger by lazy {
        val logDir = System.getenv("KINFRA_LOG_DIR") ?: "logs"
        val logLevelStr = System.getenv("KINFRA_LOG_LEVEL") ?: "INFO"
        val logLevel =
            try {
                LogLevel.valueOf(logLevelStr.uppercase())
            } catch (_: IllegalArgumentException) {
                LogLevel.INFO
            }
        getLogger()
    }

    val envFileLoader: EnvFileLoader by lazy { EnvFileLoaderImpl() }
    val fileRepository by lazy { FileRepositoryImpl() }
    val processExecutor by lazy { ProcessExecutorImpl() }
    val globalConfigCompleter: GlobalConfigCompleter by lazy { GlobalConfigCompleterImpl(filePaths) }
    val configRepository: ConfigRepository by lazy { ConfigRepositoryImpl(filePaths, kogger, globalConfigCompleter) }
    val terraformRepository by lazy { TerraformRepositoryImpl(fileRepository, loginRepo) }

    val bitwardenRepository: BitwardenRepository by lazy { BitwardenRepositoryImpl(processExecutor, filePaths) }

    val globalConfig: GlobalConfig by lazy {
        configRepository.loadGlobalConfig()
    }

    val versionChecker: VersionChecker by lazy { VersionCheckerImpl(kogger) }
    val autoUpdater: AutoUpdater by lazy {
        AutoUpdaterImpl(kogger, filePaths)
    }
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

    val commandInterpreter: CommandInterpreter by lazy { CommandInterpreter(kogger) }
    val systemRequirement: SystemRequirement by lazy { SystemRequirement(kogger) }
    val updateHandler: UpdateHandler by lazy {
        UpdateHandler(
            versionChecker, autoUpdater, kogger, configRepository, loginRepo
        )
    }

    val subProjectExecutor: SubProjectExecutor by lazy { SubProjectExecutor(configRepository, loginRepo) }


    // Actions (without HelpAction first to avoid circular dependency)
    private fun actionsWithoutHelp(terraformService: TerraformService): Map<Pair<String, SubActionType?>, Action> {
        return buildMap {
            // Regular actions
            put(Pair(ActionType.FMT.actionName, null), FormatAction(terraformService, gitHelper))
            put(Pair(ActionType.VALIDATE.actionName, null), ValidateAction(terraformService, gitHelper))
            put(Pair(ActionType.STATUS.actionName, null), StatusAction(terraformService, gitHelper))
            put(
                Pair(ActionType.LOGIN.actionName, null),
                LoginAction(
                    bitwardenRepository,
                    configRepository,
                    gitHelper,
                    filePaths,
                    loginRepo,
                ),
            )
            put(Pair(ActionType.HELLO.actionName, null), HelloAction(terraformService, kogger, gitHelper))
            put(Pair(ActionType.INIT.actionName, null), InitAction(terraformService, gitHelper))
            put(Pair(ActionType.PLAN.actionName, null), PlanAction(terraformService, gitHelper, subProjectExecutor))
            put(Pair(ActionType.APPLY.actionName, null), ApplyAction(terraformService))
            put(Pair(ActionType.DESTROY.actionName, null), DestroyAction(terraformService, gitHelper))
            put(
                Pair(ActionType.DEPLOY.actionName, null),
                DeployAction(
                    terraformService,
                    configRepository,
                    loginRepo,
                    kogger,
                ),
            )
            put(Pair(ActionType.PUSH.actionName, null), PushAction(gitHelper))
            put(Pair(ActionType.CONFIG.actionName, null), ConfigAction(loginRepo))
            put(Pair(ActionType.CONFIG_EDIT.actionName, null), ConfigEditAction(loginRepo, kogger))
            put(
                Pair(ActionType.SELF_UPDATE.actionName, null),
                SelfUpdateAction(
                    versionChecker,
                    autoUpdater,
                    gitHelper,
                    loginRepo,
                ),
            )
            put(
                Pair(ActionType.CURRENT.actionName, SubActionType.GENERATE),
                CurrentGenerateVariableAction(configRepository)
            )
            put(Pair(ActionType.CURRENT.actionName, SubActionType.PLAN), CurrentPlanAction(configRepository))
            put(Pair(ActionType.NEXT.actionName, null), NextAction(processExecutor, loginRepo, kogger))
            put(Pair(ActionType.SUBMODULE.actionName, null), SubmoduleAction(processExecutor, kogger))

            // Subcommands
            put(Pair(ActionType.SUB.actionName, SubActionType.LIST), SubListAction(loginRepo))
            put(Pair(ActionType.SUB.actionName, SubActionType.ADD), SubAddAction(loginRepo))
            put(
                Pair(ActionType.SUB.actionName, SubActionType.SHOW),
                SubShowAction(
                    configRepository,
                    filePaths,
                    loginRepo,
                ),
            )
            put(
                Pair(ActionType.SUB.actionName, SubActionType.EDIT),
                SubEditAction(
                    loginRepo,
                    kogger,
                ),
            )
            put(Pair(ActionType.SUB.actionName, SubActionType.REMOVE), SubRemoveAction(loginRepo))
            put(
                Pair(ActionType.SUB.actionName, SubActionType.PLAN),
                SubPlanAction(
                    loginRepo,
                    subProjectExecutor,
                ),
            )

            // SDK-based actions (only if BWS_ACCESS_TOKEN is available)
            if (hasBwsToken && bitwardenSecretManagerRepository != null) {
                put(
                    Pair(ActionType.DEPLOY_SDK.actionName, null),
                    DeployActionWithSDK(
                        terraformService,
                        configRepository,
                        loginRepo,
                        kogger,
                    ),
                )
            }
        }
    }

    // All actions including HelpAction
    private fun actions(terraformService: TerraformService): Map<Pair<String, SubActionType?>, Action> {
        val actionsMap = actionsWithoutHelp(terraformService)
        return actionsMap.toMutableMap().apply {
            // Help action needs access to all actions (without help itself)
            val actionsForHelp =
                buildMap {
                    ActionType.entries.forEach { actionType ->
                        if (actionType == ActionType.SUB || actionType == ActionType.CURRENT) {
                            SubActionType.entries.forEach { subActionType ->
                                actionsMap[Pair(actionType.actionName, subActionType)]?.let {
                                    put("${actionType.actionName} ${subActionType.actionName}", it)
                                }
                            }
                        } else if (actionType != ActionType.HELP) {
                            actionsMap[Pair(actionType.actionName, null)]?.let {
                                put(actionType.actionName, it)
                            }
                        }
                    }
                }
            put(Pair(ActionType.HELP.actionName, null), HelpAction(actionsForHelp, gitHelper))
        }
    }

    fun getAction(
        actionName: String,
        subActionType: SubActionType? = null,
        terraformService: TerraformService,
    ): Action? {
        val actions = actions(terraformService)
        return actions[Pair(actionName, subActionType)]
    }

}
