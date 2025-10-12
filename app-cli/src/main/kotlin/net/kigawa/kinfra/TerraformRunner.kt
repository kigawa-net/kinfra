package net.kigawa.kinfra

import net.kigawa.kinfra.infrastructure.logging.Logger
import net.kigawa.kinfra.infrastructure.update.VersionChecker
import net.kigawa.kinfra.infrastructure.update.AutoUpdater
import net.kigawa.kinfra.infrastructure.config.ConfigRepository
import net.kigawa.kinfra.model.Command
import net.kigawa.kinfra.model.CommandType
import net.kigawa.kinfra.util.AnsiColors
import net.kigawa.kinfra.util.VersionUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import kotlin.system.exitProcess

class TerraformRunner : KoinComponent {
    private val logger: Logger by inject()

    private val commands: Map<String, Command> by lazy {
        buildMap {
            CommandType.entries.forEach { commandType ->
                runCatching {
                    val command: Command by inject(named(commandType.commandName))
                    put(commandType.commandName, command)
                }
            }
        }
    }

    fun run(args: Array<String>) {
        logger.info("Starting Terraform Runner with args: ${args.joinToString(" ")}")

        if (args.isEmpty()) {
            logger.warn("No command provided")
            commands[CommandType.HELP.commandName]?.execute(emptyArray())
            exitProcess(1)
        }

        var commandName = args[0]
        logger.debug("Original command: $commandName")

        // deploy と setup-r2 コマンドは常に SDK 版を使用
        when (commandName) {
            CommandType.DEPLOY.commandName -> {
                commandName = CommandType.DEPLOY_SDK.commandName
                logger.info("Command redirected to SDK version: $commandName")
            }
            CommandType.SETUP_R2.commandName -> {
                commandName = CommandType.SETUP_R2_SDK.commandName
                logger.info("Command redirected to SDK version: $commandName")
            }
        }

        val command = commands[commandName]

        if (command == null) {
            // SDK版コマンドが見つからない場合、BWS_ACCESS_TOKENの設定を促す
            if (commandName == CommandType.DEPLOY_SDK.commandName || commandName == CommandType.SETUP_R2_SDK.commandName) {
                logger.error("BWS_ACCESS_TOKEN is not set for SDK command: $commandName")
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} BWS_ACCESS_TOKEN is not set.")
                println()
                println("${AnsiColors.BLUE}Secret Manager is required for this command.${AnsiColors.RESET}")
                println("${AnsiColors.BLUE}Please set the BWS_ACCESS_TOKEN environment variable:${AnsiColors.RESET}")
                println("  export BWS_ACCESS_TOKEN=\"your-token\"")
                println()
                println("${AnsiColors.BLUE}To generate a token:${AnsiColors.RESET}")
                println("  1. Log in to Bitwarden Web Vault")
                println("  2. Go to Secret Manager section")
                println("  3. Generate an access token from project settings")
                exitProcess(1)
            }

            logger.error("Unknown command: $commandName")
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Unknown command: $commandName")
            commands[CommandType.HELP.commandName]?.execute(emptyArray())
            exitProcess(1)
        }

        // Skip Terraform check for help, login, config, hello, setup-r2 and self-update commands
        val skipTerraformCheck = commandName == CommandType.HELP.commandName
            || commandName == CommandType.LOGIN.commandName
            || commandName == CommandType.CONFIG.commandName
            || commandName == CommandType.HELLO.commandName
            || commandName == CommandType.SETUP_R2.commandName
            || commandName == CommandType.SETUP_R2_SDK.commandName
            || commandName == CommandType.SELF_UPDATE.commandName
        if (!skipTerraformCheck && !isTerraformInstalled()) {
            logger.error("Terraform is not installed")
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Terraform is not installed or not found in PATH.")
            println("${AnsiColors.BLUE}Please install Terraform:${AnsiColors.RESET}")
            println("  Ubuntu/Debian: sudo apt-get install terraform")
            println("  macOS: brew install terraform")
            println("  Or download from: https://www.terraform.io/downloads.html")
            exitProcess(1)
        }

        val commandArgs = args.drop(1).toTypedArray()

        logger.info("Executing command: $commandName with args: ${commandArgs.joinToString(" ")}")
        val exitCode = command.execute(commandArgs)
        logger.info("Command $commandName finished with exit code: $exitCode")

        // Check for updates after command execution
        checkForUpdates()

        if (exitCode != 0) {
            logger.error("Command $commandName failed with exit code: $exitCode")
            exitProcess(exitCode)
        }
    }

    private fun checkForUpdates() {
        try {
            val configRepository: ConfigRepository by inject()
            val versionChecker: VersionChecker by inject()
            val autoUpdater: AutoUpdater by inject()

            // Load config to check if auto-update is enabled
            val config = runCatching {
                configRepository.loadKinfraConfig()
            }.getOrNull()

            // Skip update check if auto-update is disabled
            if (config?.update?.autoUpdate == false) {
                logger.debug("Auto-update is disabled in config")
                return
            }

            val updateSettings = config?.update ?: return
            val lastCheckTime = autoUpdater.getLastCheckTime()

            // Check if we should check for updates based on interval
            if (!versionChecker.shouldCheckForUpdate(lastCheckTime, updateSettings.checkInterval)) {
                logger.debug("Skipping update check - not enough time has passed since last check")
                return
            }

            // Get current version from build-time generated properties
            val currentVersion = VersionUtil.getVersion()

            logger.debug("Checking for updates - current version: $currentVersion")

            val versionInfo = versionChecker.checkForUpdates(currentVersion, updateSettings.githubRepo)
            autoUpdater.updateLastCheckTime()

            if (versionInfo.updateAvailable) {
                println("\n${AnsiColors.YELLOW}Update available!${AnsiColors.RESET} New version ${versionInfo.latestVersion} is available (current: ${versionInfo.currentVersion})")
                println("${AnsiColors.BLUE}Downloading and installing update...${AnsiColors.RESET}")

                val updateSuccess = autoUpdater.performUpdate(versionInfo)
                if (!updateSuccess) {
                    println("${AnsiColors.YELLOW}Automatic update failed. Please update manually:${AnsiColors.RESET}")
                    println("  curl -fsSL https://raw.githubusercontent.com/${updateSettings.githubRepo}/main/install.sh | bash")
                }
            }
        } catch (e: Exception) {
            logger.warn("Error during update check: ${e.message}")
            // Silently fail - don't interrupt user workflow
        }
    }

    private fun isTerraformInstalled(): Boolean {
        return try {
            val process = ProcessBuilder("terraform", "version")
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
            process.waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }
}
