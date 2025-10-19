package net.kigawa.kinfra

import net.kigawa.kinfra.action.logging.Logger
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
            commandInterpreter.handleUnknownAction(parsedCommand.actionName) {
                actionRegistry.getHelpAction()?.execute(emptyList())
            }
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
}