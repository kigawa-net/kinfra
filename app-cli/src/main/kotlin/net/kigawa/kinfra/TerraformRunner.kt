package net.kigawa.kinfra

import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.model.ActionType
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.service.ActionRegistry
import net.kigawa.kinfra.service.CommandInterpreter
import net.kigawa.kinfra.service.SystemRequirement
import net.kigawa.kinfra.service.UpdateHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.system.exitProcess

class TerraformRunner: KoinComponent {
    private val logger: Logger by inject()
    private val actionRegistry: ActionRegistry by inject()
    private val commandInterpreter: CommandInterpreter by inject()
    private val systemRequirement: SystemRequirement by inject()
    private val updateHandler: UpdateHandler by inject()

    fun run(args: Array<String>) {
        logger.info("Starting Terraform Runner with args: ${args.joinToString(" ")}")

        // Parse command line arguments
        val parsedCommand = commandInterpreter.parse(args)
            ?: run {
                actionRegistry.getHelpAction()?.execute(emptyList())
                exitProcess(1)
            }

        // Get the action
        val action = actionRegistry.getAction(parsedCommand.actionName, parsedCommand.subActionType)

        if (action == null) {
            handleUnknownAction(parsedCommand.actionName)
            return
        }

        // Handle help flag
        if (parsedCommand.showHelp) {
            action.showHelp()
            exitProcess(0)
        }

        // Validate Terraform installation if needed
        if (!commandInterpreter.shouldSkipTerraformCheck(parsedCommand.actionName)) {
            if (!systemRequirement.validateTerraformInstallation()) {
                exitProcess(1)
            }
        }

        // Execute the action
        logger.info(
            "Executing action: ${parsedCommand.actionName} with args: ${parsedCommand.actionArgs.joinToString(" ")}"
        )
        val exitCode = action.execute(parsedCommand.actionArgs)
        logger.info("Action ${parsedCommand.actionName} finished with exit code: $exitCode")

        // Check for updates after action execution
        updateHandler.checkForUpdates()

        if (exitCode != 0) {
            logger.error("Action ${parsedCommand.actionName} failed with exit code: $exitCode")
            exitProcess(exitCode)
        }
    }

    private fun handleUnknownAction(actionName: String) {
        // SDK版アクションが見つからない場合、BWS_ACCESS_TOKENの設定を促す
        if (actionName == ActionType.DEPLOY_SDK.actionName) {
            logger.error("BWS_ACCESS_TOKEN is not set for SDK action: $actionName")
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} BWS_ACCESS_TOKEN is not set.")
            println()
            println("${AnsiColors.BLUE}Secret Manager is required for this action.${AnsiColors.RESET}")
            println("${AnsiColors.BLUE}Please set the BWS_ACCESS_TOKEN environment variable:${AnsiColors.RESET}")
            println("  export BWS_ACCESS_TOKEN=\"your-token\"")
            println()
            println("${AnsiColors.BLUE}To generate a token:${AnsiColors.RESET}")
            println("  1. Log in to Bitwarden Web Vault")
            println("  2. Go to Secret Manager section")
            println("  3. Generate an access token from project settings")
            exitProcess(1)
        }

        // config-editアクションが見つからない場合、代替案を提示
        if (actionName == ActionType.CONFIG_EDIT.actionName) {
            logger.error("config-edit action not found: $actionName")
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Unknown action: $actionName")
            println()
            println("${AnsiColors.BLUE}Did you mean:${AnsiColors.RESET}")
            println("  kinfra config          - Edit configuration files")
            println("  kinfra config edit     - Edit configuration files (alternative)")
            println("  kinfra config --parent  - Edit parent configuration")
            println()
            println("${AnsiColors.BLUE}Available commands:${AnsiColors.RESET}")
            actionRegistry.getHelpAction()?.execute(emptyList())
            exitProcess(1)
        }

        logger.error("Unknown action: $actionName")
        println("${AnsiColors.RED}Error:${AnsiColors.RESET} Unknown action: $actionName")
        actionRegistry.getHelpAction()?.execute(emptyList())
        exitProcess(1)
    }
}
