package net.kigawa.kinfra

import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.action.update.AutoUpdater
import net.kigawa.kinfra.action.update.VersionChecker
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.ActionType
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.VersionUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import kotlin.system.exitProcess

class TerraformRunner(
    val loginRepo: LoginRepo
) : KoinComponent {
    private val logger: Logger by inject()

    private val actions: Map<String, Action> by lazy {
        buildMap {
            ActionType.entries.forEach { actionType ->
                runCatching {
                    val action: Action by inject(named(actionType.actionName))
                    put(actionType.actionName, action)
                }.onFailure { e ->
                    logger.warn("Failed to register action ${actionType.actionName}: ${e.message}")
                }
            }
        }
    }

    fun run(args: Array<String>) {
        logger.info("Starting Terraform Runner with args: ${args.joinToString(" ")}")

        if (args.isEmpty()) {
            logger.warn("No action provided")
            actions[ActionType.HELP.actionName]?.execute(emptyArray())
            exitProcess(1)
        }

        var actionName = args[0]
        logger.debug("Original action: $actionName")

        // Map --help and -h flags to help action
        if (actionName == "--help" || actionName == "-h") {
            actionName = ActionType.HELP.actionName
            logger.debug("Mapped $actionName to help action")
        }

        // deploy アクションは常に SDK 版を使用
        when (actionName) {
            ActionType.DEPLOY.actionName -> {
                actionName = ActionType.DEPLOY_SDK.actionName
                logger.info("Action redirected to SDK version: $actionName")
            }
        }

        val action = actions[actionName]

        if (action == null) {
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

            logger.error("Unknown action: $actionName")
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Unknown action: $actionName")
            actions[ActionType.HELP.actionName]?.execute(emptyArray())
            exitProcess(1)
        }

        // Check if --help or -h is in the arguments (but not the first argument)
        val actionArgs = args.drop(1).toTypedArray()
        if (actionArgs.isNotEmpty() && (actionArgs[0] == "--help" || actionArgs[0] == "-h")) {
            logger.debug("Showing help for action: $actionName")
            action.showHelp()
            exitProcess(0)
        }

        // Skip Terraform check for help, login, hello, self-update, push and config actions
        val skipTerraformCheck = actionName == ActionType.HELP.actionName
            || actionName == ActionType.LOGIN.actionName
            || actionName == ActionType.HELLO.actionName
            || actionName == ActionType.SELF_UPDATE.actionName
            || actionName == ActionType.PUSH.actionName
            || actionName == ActionType.CONFIG_EDIT.actionName
        if (!skipTerraformCheck && !isTerraformInstalled()) {
            logger.error("Terraform is not installed")
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Terraform is not installed or not found in PATH.")
            println("${AnsiColors.BLUE}Please install Terraform:${AnsiColors.RESET}")
            println("  Ubuntu/Debian: sudo apt-get install terraform")
            println("  macOS: brew install terraform")
            println("  Or download from: https://www.terraform.io/downloads.html")
            exitProcess(1)
        }

        logger.info("Executing action: $actionName with args: ${actionArgs.joinToString(" ")}")
        val exitCode = action.execute(actionArgs)
        logger.info("Action $actionName finished with exit code: $exitCode")

        // Check for updates after action execution
        checkForUpdates()

        if (exitCode != 0) {
            logger.error("Action $actionName failed with exit code: $exitCode")
            exitProcess(exitCode)
        }
    }

    private fun checkForUpdates() {
        try {
            val versionChecker: VersionChecker by inject()
            val autoUpdater: AutoUpdater by inject()

            // Load config to check if auto-update is enabled
            val config = runCatching {
                loginRepo.loadKinfraConfig()
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
