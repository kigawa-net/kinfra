package net.kigawa.kinfra.di

import net.kigawa.kinfra.action.actions.DeployActionWithSDK
import net.kigawa.kinfra.action.actions.HelpAction
import net.kigawa.kinfra.action.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.infrastructure.file.SystemHomeDirGetter
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.ActionType
import net.kigawa.kinfra.model.SubActionType
import net.kigawa.kinfra.model.conf.FilePaths
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    // Include all sub-modules
    includes(infrastructureModule, bitwardenModule, actionsModule)

    // Check if BWS_ACCESS_TOKEN is available for SDK actions
    val filePathsInstance = FilePaths(SystemHomeDirGetter())
    val hasBwsToken = System.getenv("BWS_ACCESS_TOKEN") != null ||
            java.io.File(filePathsInstance.bwsTokenFileName).let { it.exists() && it.canRead() && it.readText().trim().isNotBlank() }

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
                            put("${actionType.actionName} ${subActionType.actionName}", get<Action>(named("${actionType.actionName} ${subActionType.actionName}")))
                        }
                    }
                } else if (actionType != ActionType.HELP) {
                    runCatching {
                        put(actionType.actionName, get<Action>(named(actionType.actionName)))
                    }
                }
            }
        }

        HelpAction(actionMap, get())
    }
}