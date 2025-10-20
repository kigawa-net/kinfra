package net.kigawa.kinfra.action.execution

import net.kigawa.kinfra.model.GitHelper
import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.action.update.AutoUpdater
import net.kigawa.kinfra.action.update.VersionChecker
import net.kigawa.kinfra.action.update.VersionInfo
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.VersionUtil

/**
 * アップデート処理の各機能を担当するクラス
 */
class UpdateProcessor(
    private val versionChecker: VersionChecker,
    private val autoUpdater: AutoUpdater,
    private val gitHelper: GitHelper,
    private val loginRepo: LoginRepo,
    private val logger: Logger
) {
    
    fun performUpdate(args: List<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

        println("${AnsiColors.BLUE}=== Kinfra Self-Update ===${AnsiColors.RESET}")
        println()

        val currentVersion = VersionUtil.getVersion()
        println("${AnsiColors.BLUE}Current version:${AnsiColors.RESET} $currentVersion")
        println()

        val updateSettings = getUpdateSettings() ?: return 1
        val githubRepo = updateSettings.githubRepo ?: "kigawa-net/kinfra"

        val versionInfo = checkForUpdates(githubRepo) ?: return 1
        
        if (!versionInfo.updateAvailable) {
            println("${AnsiColors.GREEN}✓${AnsiColors.RESET} You are already on the latest version: ${versionInfo.currentVersion}")
            return 0
        }

        return if (shouldUpdate(args)) {
            executeUpdate(versionInfo, githubRepo)
        } else {
            println("${AnsiColors.YELLOW}Update cancelled${AnsiColors.RESET}")
            0
        }
    }
    
    private fun getUpdateSettings() = try {
        loginRepo.loadKinfraConfig()?.update
    } catch (e: Exception) {
        println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to load configuration: ${e.message}")
        null
    }
    
    private fun checkForUpdates(githubRepo: String) = try {
        println("${AnsiColors.BLUE}Checking for updates from${AnsiColors.RESET} $githubRepo...")
        println()
        
        val currentVersion = VersionUtil.getVersion()
        versionChecker.checkForUpdates(currentVersion, githubRepo)
    } catch (e: Exception) {
        println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to check for updates: ${e.message}")
        null
    }
    
    private fun shouldUpdate(args: List<String>): Boolean {
        val force = args.contains("--force") || args.contains("-f")
        if (!force) {
            print("${AnsiColors.BLUE}Do you want to update?${AnsiColors.RESET} (Y/n): ")
            val response = readLine()?.trim()?.lowercase()
            if (response != "" && response != "y" && response != "yes") {
                return false
            }
            println()
        }
        return true
    }
    
    private fun executeUpdate(versionInfo: VersionInfo, githubRepo: String): Int {
        println("${AnsiColors.YELLOW}Update available!${AnsiColors.RESET}")
        println("  Current version: ${versionInfo.currentVersion}")
        println("  Latest version:  ${versionInfo.latestVersion}")
        println()

        println("${AnsiColors.BLUE}Downloading and installing update...${AnsiColors.RESET}")
        println()

        val updateSuccess = autoUpdater.performUpdate(versionInfo)

        return if (updateSuccess) {
            println("${AnsiColors.GREEN}✓ Update completed successfully!${AnsiColors.RESET}")
            println()
            println("${AnsiColors.YELLOW}Please restart kinfra to use the new version${AnsiColors.RESET}")
            0
        } else {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to update automatically.")
            println()
            println("${AnsiColors.BLUE}You can update manually using:${AnsiColors.RESET}")
            println("  curl -fsSL https://raw.githubusercontent.com/$githubRepo/main/install.sh | bash")
            1
        }
    }
}