package net.kigawa.kinfra

import net.kigawa.kinfra.di.DependencyContainer
import net.kigawa.kinfra.infrastructure.file.SystemHomeDirGetter
import kotlin.system.exitProcess

class TerraformRunner(private val container: DependencyContainer) {
    private val logger = container.logger
    private val actionRegistry = container.actionRegistry
    private val commandInterpreter = container.commandInterpreter
    private val systemRequirement = container.systemRequirement
    private val updateHandler = container.updateHandler

    fun run(args: Array<String>) {
        logger.info("Starting Terraform Runner with args: ${args.joinToString(" ")}")

        // Parse command line arguments
        val parsedCommand = commandInterpreter.parse(args)
            ?: run {
                actionRegistry.getHelpAction()?.execute(emptyList())
                exitProcess(1)
            }

        // Set default working directory if not specified
        val workingDir = parsedCommand.workingDir ?: getDefaultWorkingDir()
        logger.debug("Using working directory: $workingDir")
        
        // Set system property for working directory
        System.setProperty("user.dir", workingDir)

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
            "Executing action: ${parsedCommand.actionName} with args: ${parsedCommand.actionArgs.joinToString(" ")} in directory: $workingDir"
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

    private fun getDefaultWorkingDir(): String {
        val homeDirGetter = SystemHomeDirGetter()
        val homeDir = homeDirGetter.getHomeDir()
        val kinfraDir = "$homeDir/.local/kinfra/repos"
        
        // Try to find the first repository directory
        val reposDir = java.io.File(kinfraDir)
        if (reposDir.exists() && reposDir.isDirectory) {
            val firstRepo = reposDir.listFiles()?.find { it.isDirectory && java.io.File(it, ".git").exists() }
            if (firstRepo != null) {
                return firstRepo.absolutePath
            }
        }
        
        // Fallback to current directory
        return System.getProperty("user.dir")
    }
}