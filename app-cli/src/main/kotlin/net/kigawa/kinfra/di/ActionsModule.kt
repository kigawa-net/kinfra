package net.kigawa.kinfra.di

import net.kigawa.kinfra.action.actions.*
import net.kigawa.kinfra.actions.LoginAction
import net.kigawa.kinfra.action.actions.SubEditAction
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.ActionType
import net.kigawa.kinfra.model.SubActionType
import org.koin.core.qualifier.named
import org.koin.dsl.module

val actionsModule = module {

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
    single<Action>(named(ActionType.DEPLOY.actionName)) { DeployAction(get(), get(), get(), get()) }
    single<Action>(named(ActionType.PUSH.actionName)) { PushAction(get()) }
    single<Action>(named("${ActionType.SUB.actionName} ${SubActionType.LIST.actionName}")) { SubListAction(get()) }
    single<Action>(named("${ActionType.SUB.actionName} ${SubActionType.ADD.actionName}")) { SubAddAction(get()) }
    single<Action>(named("${ActionType.SUB.actionName} ${SubActionType.SHOW.actionName}")) { SubShowAction(get(), get(), get()) }
    single<Action>(named("${ActionType.SUB.actionName} ${SubActionType.EDIT.actionName}")) { SubEditAction(get(), get()) }
    single<Action>(named("${ActionType.SUB.actionName} ${SubActionType.REMOVE.actionName}")) { SubRemoveAction(get()) }
    single<Action>(named("${ActionType.SUB.actionName} ${SubActionType.PLAN.actionName}")) { SubPlanAction(get(), get()) }
    single<Action>(named(ActionType.CONFIG.actionName)) { ConfigAction(get()) }
    single<Action>(named(ActionType.CONFIG_EDIT.actionName)) { ConfigEditAction(get(), get()) }
    single<Action>(named(ActionType.SELF_UPDATE.actionName)) { SelfUpdateAction(get(), get(), get(), get(), get()) }

    // SDK-based actions (only if BWS_ACCESS_TOKEN is available)
    // Note: This will be conditionally registered in the main appModule
}