package net.kigawa.kinfra.service

import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.action.update.AutoUpdater
import net.kigawa.kinfra.action.update.VersionChecker
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.VersionUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UpdateHandler(
    private val loginRepo: LoginRepo
) : KoinComponent {
    private val logger: Logger by inject()
    private val versionChecker: VersionChecker by inject()
    private val autoUpdater: AutoUpdater by inject()

    fun checkForUpdates() {
        try {
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
}