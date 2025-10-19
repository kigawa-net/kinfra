package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.action.update.VersionChecker
import net.kigawa.kinfra.action.update.AutoUpdater
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.VersionUtil

class SelfUpdateAction(
    private val versionChecker: VersionChecker,
    private val autoUpdater: AutoUpdater,
    private val gitHelper: GitHelper,
    val loginRepo: LoginRepo
) : Action {

    override fun execute(args: List<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

        println("${AnsiColors.BLUE}=== Kinfra Self-Update ===${AnsiColors.RESET}")
        println()

        // Get current version
        val currentVersion = VersionUtil.getVersion()
        println("${AnsiColors.BLUE}Current version:${AnsiColors.RESET} $currentVersion")
        println()

        // Load config to get GitHub repo
        val config = try {
            loginRepo.loadKinfraConfig()
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to load configuration: ${e.message}")
            return 1
        }

        if (config == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Configuration not found. Please run 'kinfra login' first.")
            return 1
        }

        val updateSettings = config.update
        val githubRepo = updateSettings?.githubRepo ?: "kigawa-net/kinfra"

        // Check for updates
        println("${AnsiColors.BLUE}Checking for updates from${AnsiColors.RESET} $githubRepo...")
        println()

        val versionInfo = try {
            versionChecker.checkForUpdates(currentVersion, githubRepo)
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to check for updates: ${e.message}")
            return 1
        }

        if (!versionInfo.updateAvailable) {
            println("${AnsiColors.GREEN}✓${AnsiColors.RESET} You are already on the latest version: ${versionInfo.currentVersion}")
            return 0
        }

        // Update available
        println("${AnsiColors.YELLOW}Update available!${AnsiColors.RESET}")
        println("  Current version: ${versionInfo.currentVersion}")
        println("  Latest version:  ${versionInfo.latestVersion}")
        println()

        // Prompt for confirmation unless --force is specified
        val force = args.contains("--force") || args.contains("-f")
        if (!force) {
            print("${AnsiColors.BLUE}Do you want to update?${AnsiColors.RESET} (Y/n): ")
            val response = readLine()?.trim()?.lowercase()
            if (response != "" && response != "y" && response != "yes") {
                println("${AnsiColors.YELLOW}Update cancelled${AnsiColors.RESET}")
                return 0
            }
            println()
        }

        // Perform update
        println("${AnsiColors.BLUE}Downloading and installing update...${AnsiColors.RESET}")
        println()

        val updateSuccess = autoUpdater.performUpdate(versionInfo)

        if (updateSuccess) {
            println("${AnsiColors.GREEN}✓ Update completed successfully!${AnsiColors.RESET}")
            println()
            println("${AnsiColors.YELLOW}Please restart kinfra to use the new version${AnsiColors.RESET}")
            return 0
        } else {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to update automatically.")
            println()
            println("${AnsiColors.BLUE}You can update manually using:${AnsiColors.RESET}")
            println("  curl -fsSL https://raw.githubusercontent.com/$githubRepo/main/install.sh | bash")
            return 1
        }
    }

    override fun getDescription(): String {
        return "Update kinfra to the latest version"
    }
}
