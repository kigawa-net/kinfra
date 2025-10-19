package net.kigawa.kinfra.service

import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.ActionType
import net.kigawa.kinfra.model.SubActionType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class ActionRegistry: KoinComponent {
    private val logger: Logger by inject()

    private val actions: Map<Pair<String, SubActionType?>, Action> by lazy {
        val map = mutableMapOf<Pair<String, SubActionType?>, Action>()
        ActionType.entries.forEach { actionType ->
            if (actionType == ActionType.SUB) {
                // Handle subcommands
                SubActionType.entries.forEach { subActionType ->
                    runCatching {
                        val action: Action by inject(named("${actionType.actionName} ${subActionType.actionName}"))
                        map[Pair(actionType.actionName, subActionType)] = action
                    }.onFailure { e ->
                        logger.warn(
                            "Failed to register subaction ${actionType.actionName} ${subActionType.actionName}: ${e.message}"
                        )
                    }
                }
            } else {
                runCatching {
                    val action: Action by inject(named(actionType.actionName))
                    map[Pair(actionType.actionName, null)] = action
                }.onFailure { e ->
                    logger.warn("Failed to register action ${actionType.actionName}: ${e.message}")
                }
            }
        }
        map
    }

    fun getAction(actionName: String, subActionType: SubActionType? = null): Action? {
        return actions[Pair(actionName, subActionType)]
    }

    fun getHelpAction(): Action? {
        return actions[Pair(ActionType.HELP.actionName, null)]
    }

}