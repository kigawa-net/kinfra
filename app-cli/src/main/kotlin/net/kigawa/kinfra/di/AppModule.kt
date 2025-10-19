package net.kigawa.kinfra.di

import net.kigawa.kinfra.TerraformRunner
import net.kigawa.kinfra.action.actions.*
import net.kigawa.kinfra.action.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.action.execution.ActionExecutor
import net.kigawa.kinfra.actions.LoginAction
import net.kigawa.kinfra.infrastructure.bitwarden.BitwardenSecretManagerRepositoryImpl
import net.kigawa.kinfra.infrastructure.config.EnvFileLoaderImpl
import net.kigawa.kinfra.infrastructure.file.SystemHomeDirGetter
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.ActionType
import net.kigawa.kinfra.model.SubActionType
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.service.ActionRegistry
import net.kigawa.kinfra.service.CommandInterpreter
import net.kigawa.kinfra.service.SystemRequirement
import net.kigawa.kinfra.service.UpdateHandler
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    // Include all sub-modules
    includes(infrastructureModule, bitwardenModule, actionsModule)

    // Check if BWS_ACCESS_TOKEN is available for SDK actions
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

    // Service layer components
    single { ActionRegistry() }
    single { CommandInterpreter() }
    single { SystemRequirement() }
    single { UpdateHandler(get()) }
    
    
    
    // Execution layer components
    single { ActionExecutor(get()) }

    // Presentation layer
    single<TerraformRunner> { TerraformRunner() }

    // Actions
    single<Action>(named(ActionType.FMT.actionName)) { FormatAction(get(), get()) }
    single<Action>(named(ActionType.VALIDATE.actionName)) { ValidateAction(get(), get()) }
    single<Action>(named(ActionType.STATUS.actionName)) { StatusAction(get(), get()) }
    single<Action>(named(ActionType.LOGIN.actionName)) { LoginAction(get(), get(), get(), get(), get()) }
    single<Action>(named(ActionType.HELLO.actionName)) { HelloAction(get(), get(), get()) }
    single<Action>(named(ActionType.INIT.actionName)) { InitAction(get(), get()) }
    single<Action>(named(ActionType.PLAN.actionName)) { PlanAction(get(), get()) }
    single<Action>(named(ActionType.APPLY.actionName)) { ApplyAction(get()) }
    single<Action>(named(ActionType.DESTROY.actionName)) { DestroyAction(get(), get()) }
    single<Action>(named(ActionType.DEPLOY.actionName)) { DeployAction(get(), get(), get()) }
    single<Action>(named(ActionType.PUSH.actionName)) { PushAction(get()) }
    single<Action>(named(ActionType.SELF_UPDATE.actionName)) {
        SelfUpdateAction(
            get(), get(), get(), get(), get()
        )
    }

    // SDK-based actions (only if BWS_ACCESS_TOKEN is available)
    if (hasBwsToken) {
        single<Action>(named(ActionType.DEPLOY_SDK.actionName)) { DeployActionWithSDK(get(), get(), get(), get()) }
    }

    // Help action needs access to all actions
    single<Action>(named(ActionType.HELP.actionName)) {
        val actionMap = buildMap<String, Action> {
            ActionType.entries.forEach { actionType ->
                if (actionType == ActionType.SUB) {
                    // Add subcommands
                    SubActionType.entries.forEach { subActionType ->
                        runCatching {
                            put(
                                "${actionType.actionName} ${subActionType.actionName}",
                                get<Action>(named("${actionType.actionName} ${subActionType.actionName}"))
                            )
                        }
                    }
                } else if (actionType != ActionType.HELP) {
                    runCatching {
                        put(actionType.actionName, get<Action>(named(actionType.actionName)))
                    }.onFailure { e ->
                        println("DEBUG: Failed to add action ${actionType.actionName}: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
        }

        HelpAction(actionMap, get())
    }
}