package net.kigawa.kinfra.service

import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.model.ActionType
import net.kigawa.kinfra.model.SubActionType
import net.kigawa.kinfra.model.util.AnsiColors
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.system.exitProcess

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

    fun handleUnknownAction(actionName: String, helpAction: (() -> Unit)? = null) {
        logger.error("Unknown action: $actionName")
        
        when (actionName) {
            ActionType.DEPLOY_SDK.actionName -> {
                logger.error("BWS_ACCESS_TOKEN is not set for SDK action: $actionName")
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} BWS_ACCESS_TOKEN is not set.")
                println()
                println("${AnsiColors.BLUE}Secret Manager is required for this action.${AnsiColors.RESET}")
                println("${AnsiColors.BLUE}Please set BWS_ACCESS_TOKEN environment variable:${AnsiColors.RESET}")
                println("  export BWS_ACCESS_TOKEN=\"your-token\"")
                println()
                println("${AnsiColors.BLUE}To generate a token:${AnsiColors.RESET}")
                println("  1. Log in to Bitwarden Web Vault")
                println("  2. Go to Secret Manager section")
                println("  3. Generate an access token from project settings")
                exitProcess(1)
            }
            
            ActionType.CONFIG_EDIT.actionName -> {
                logger.error("config-edit action not found: $actionName")
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Unknown action: $actionName")
                println()
                println("${AnsiColors.BLUE}Did you mean:${AnsiColors.RESET}")
                println("  kinfra config          - Edit configuration files")
                println("  kinfra config edit     - Edit configuration files (alternative)")
                println("  kinfra config --parent  - Edit parent configuration")
                println()
                println("${AnsiColors.BLUE}Available commands:${AnsiColors.RESET}")
                helpAction?.invoke()
                exitProcess(1)
            }
            
            else -> {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Unknown action: $actionName")
                helpAction?.invoke()
                exitProcess(1)
            }
        }
    }
}