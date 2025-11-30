package net.kigawa.kinfra.service

import net.kigawa.kinfra.model.ActionType
import net.kigawa.kinfra.model.SubActionType
import net.kigawa.kodel.api.log.Kogger
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kodel.api.log.traceignore.debug
import net.kigawa.kodel.api.log.traceignore.error
import net.kigawa.kodel.api.log.traceignore.warn
import kotlin.system.exitProcess

data class ParsedCommand(
    val actionName: String,
    val subActionType: SubActionType? = null,
    val actionArgs: List<String>,
    val showHelp: Boolean = false,
)

class CommandInterpreter(private val kogger: Kogger) {
    fun parse(args: Array<String>): ParsedCommand? {
        if (args.isEmpty()) {
            kogger.warn("No action provided")
            return null
        }

        var actionName = args[0]
        var subActionType: SubActionType? = null
        var actionArgs = args.drop(1)
        kogger.debug("Original action: $actionName")

        // Handle subcommands
        if ((actionName == ActionType.SUB.actionName || actionName == ActionType.CURRENT.actionName) && args.size > 1) {
            val subActionName = args[1]
            subActionType = SubActionType.fromString(subActionName)
            if (subActionType != null) {
                kogger.debug("Detected subcommand: $actionName $subActionName")
                actionArgs = args.drop(2)
            } else {
                kogger.error("Unknown subcommand: $actionName $subActionName")
                return null
            }
        }

        // Filter out --working-dir and --path options
        val workingDirIndex = actionArgs.indexOfFirst { it == "--working-dir" || it == "--path" }
        if (workingDirIndex != -1 && workingDirIndex + 1 < actionArgs.size) {
            actionArgs = actionArgs.filterIndexed { index, _ -> index != workingDirIndex && index != workingDirIndex + 1 }
            kogger.debug("Ignoring --working-dir/--path option, using logged-in repository")
        }

        // Map --help and -h flags to help action
        if (actionName == "--help" || actionName == "-h") {
            actionName = ActionType.HELP.actionName
            kogger.debug("Mapped $actionName to help action")
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
                    kogger.info("Mapped 'config edit' to config-edit action")
                }

                "add-subproject" -> {
                    // Keep as CONFIG_EDIT but don't remove subcommand
                    actionName = ActionType.CONFIG_EDIT.actionName
                    kogger.info("Mapped 'config add-subproject' to config-edit action")
                }
            }
        }

        // Handle direct config-edit command
        if (actionName == ActionType.CONFIG_EDIT.actionName) {
            kogger.debug("Direct config-edit command detected")
        }

        // deploy アクションは常に SDK 版を使用
        when (actionName) {
            ActionType.DEPLOY.actionName -> {
                actionName = ActionType.DEPLOY_SDK.actionName
                kogger.info("Action redirected to SDK version: $actionName")
            }
        }

        // Check if --help or -h is in the arguments
        val showHelp = actionArgs.contains("--help") || actionArgs.contains("-h")
        if (showHelp) {
            kogger.debug("Showing help for action: $actionName")
        }

        return ParsedCommand(
            actionName = actionName,
            subActionType = subActionType,
            actionArgs = actionArgs,
            showHelp = showHelp,
        )
    }

    fun shouldSkipTerraformCheck(actionName: String): Boolean {
        return actionName == ActionType.HELP.actionName ||
            actionName == ActionType.LOGIN.actionName ||
            actionName == ActionType.HELLO.actionName ||
            actionName == ActionType.SELF_UPDATE.actionName ||
            actionName == ActionType.PUSH.actionName ||
            actionName == ActionType.CONFIG.actionName ||
            actionName == ActionType.CONFIG_EDIT.actionName ||
            actionName == ActionType.SUB.actionName ||
            actionName == ActionType.CURRENT.actionName
    }

    fun handleUnknownAction(
        actionName: String,
        helpAction: (() -> Unit)? = null,
    ) {
        kogger.error("Unknown action: $actionName")

        when (actionName) {
            ActionType.DEPLOY_SDK.actionName -> {
                kogger.error("BWS_ACCESS_TOKEN is not set for SDK action: $actionName")
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
                kogger.error("config-edit action not found: $actionName")
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
