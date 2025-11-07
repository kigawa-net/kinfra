package net.kigawa.kinfra.service

import net.kigawa.kinfra.di.DependencyContainer
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.ActionType
import net.kigawa.kinfra.model.SubActionType
import net.kigawa.kinfra.model.service.TerraformService

class ActionRegistry(
    private val container: DependencyContainer,
    val terraformService: TerraformService,
) {
    fun getAction(
        actionName: String,
        subActionType: SubActionType? = null,
    ): Action? {
        return container.getAction(actionName, subActionType, terraformService)
    }

    fun getHelpAction(): Action? {
        return container.getAction(ActionType.HELP.actionName, null, terraformService)
    }
}
