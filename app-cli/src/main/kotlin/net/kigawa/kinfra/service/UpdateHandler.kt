package net.kigawa.kinfra.service

import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.action.update.AutoUpdater
import net.kigawa.kinfra.action.update.VersionChecker
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.conf.KinfraConfig
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.VersionUtil

class UpdateHandler(
    private val versionChecker: VersionChecker,
    private val autoUpdater: AutoUpdater,
    private val logger: Logger,
    private val configRepository: ConfigRepository,
    private val loginRepo: LoginRepo
) {

    fun checkForUpdates() {
        try {
            // Load config to check if auto-update is enabled
            val config = runCatching {
                loadCurrentConfig()
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
    
    private fun loadCurrentConfig(): KinfraConfig? {
        // Try to load from current directory first
        val currentDirConfig = runCatching {
            val configPath = configRepository.getProjectConfigFilePath()
            configRepository.loadKinfraConfig(java.nio.file.Paths.get(configPath))
        }.getOrNull()
        
        if (currentDirConfig != null) {
            return currentDirConfig
        }
        
        // Try to load from login repo if available
        return runCatching {
            loginRepo.loadKinfraConfig()
        }.getOrNull()
    }
}