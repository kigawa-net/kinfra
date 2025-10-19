package net.kigawa.kinfra.service

import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.model.ActionType
import net.kigawa.kinfra.model.SubActionType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class ParsedCommand(
    val actionName: String,
    val subActionType: SubActionType? = null,
    val actionArgs: List<String>,
    val showHelp: Boolean = false,
)

class CommandInterpreter: KoinComponent {
    private val logger: Logger by inject()

    fun parse(args: Array<String>): ParsedCommand? {
        if (args.isEmpty()) {
            logger.warn("No action provided")
            return null
        }

        var actionName = args[0]
        var subActionType: SubActionType? = null
        var actionArgs = args.drop(1)
        logger.debug("Original action: $actionName")

        // Handle subcommands
        if (actionName == ActionType.SUB.actionName && args.size > 1) {
            val subActionName = args[1]
            subActionType = SubActionType.fromString(subActionName)
            if (subActionType != null) {
                logger.debug("Detected subcommand: $actionName $subActionName")
                actionArgs = args.drop(2)
            } else {
                logger.error("Unknown subcommand: $actionName $subActionName")
                return null
            }
        }

        // Map --help and -h flags to help action
        if (actionName == "--help" || actionName == "-h") {
            actionName = ActionType.HELP.actionName
            logger.debug("Mapped $actionName to help action")
        }

// Handle config subcommands
        if (actionName == ActionType.CONFIG.actionName && actionArgs.isNotEmpty()) {
            // Find subcommand (skip flags like -p, --parent)
            val subCommand = actionArgs.find { !it.startsWith("-") }
            when (subCommand) {
                "edit" -> {
                    actionName = ActionType.CONFIG_EDIT.actionName
                    // Remove subcommand but keep flags
                    actionArgs = actionArgs.filter { it != "edit" }
                    logger.info("Mapped 'config edit' to config-edit action")
                }

                "add-subproject" -> {
                    // Keep as CONFIG_EDIT but don't remove subcommand
                    actionName = ActionType.CONFIG_EDIT.actionName
                    logger.info("Mapped 'config add-subproject' to config-edit action")
                }
            }
        }

        // Handle direct config-edit command
        if (actionName == ActionType.CONFIG_EDIT.actionName) {
            logger.debug("Direct config-edit command detected")
        }

        // deploy アクションは常に SDK 版を使用
        when (actionName) {
            ActionType.DEPLOY.actionName -> {
                actionName = ActionType.DEPLOY_SDK.actionName
                logger.info("Action redirected to SDK version: $actionName")
            }
        }

        // Check if --help or -h is in the arguments (but not the first argument)
        val showHelp = actionArgs.isNotEmpty() && (actionArgs[0] == "--help" || actionArgs[0] == "-h")
        if (showHelp) {
            logger.debug("Showing help for action: $actionName")
        }

        return ParsedCommand(
            actionName = actionName,
            subActionType = subActionType,
            actionArgs = actionArgs,
            showHelp = showHelp
        )
    }

    fun shouldSkipTerraformCheck(actionName: String): Boolean {
        return actionName == ActionType.HELP.actionName
            || actionName == ActionType.LOGIN.actionName
            || actionName == ActionType.HELLO.actionName
            || actionName == ActionType.SELF_UPDATE.actionName
            || actionName == ActionType.PUSH.actionName
            || actionName == ActionType.CONFIG.actionName
            || actionName == ActionType.CONFIG_EDIT.actionName
            || actionName == ActionType.SUB.actionName
    }
}