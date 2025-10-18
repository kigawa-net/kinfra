package net.kigawa.kinfra.infrastructure.update

import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.action.update.AutoUpdater
import net.kigawa.kinfra.action.update.VersionInfo
import net.kigawa.kinfra.model.conf.FilePaths
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class AutoUpdaterImpl(
    private val logger: Logger,
    val filePaths: FilePaths
) : AutoUpdater {
    private val appDir: File
        get() = filePaths.baseConfigDir?.toFile() ?: throw IllegalStateException("Config directory not available")
    private val jarPath: File
        get() = File(appDir, "kinfra.jar")
    private val lastCheckFile: File
        get() = File(appDir, ".last_update_check")

    override fun performUpdate(versionInfo: VersionInfo): Boolean {
        if (!versionInfo.updateAvailable) {
            logger.debug("No update available")
            return false
        }

        return try {
            logger.info("Downloading update from: ${versionInfo.downloadUrl}")

            val tempFile = File.createTempFile("kinfra-update", ".jar")
            tempFile.deleteOnExit()

            // Download new version
            val connection = URL(versionInfo.downloadUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            if (connection.responseCode == 200) {
                connection.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                logger.debug("Download completed, replacing current JAR")

                // Backup current JAR
                val backupFile = File(appDir, "kinfra.jar.backup")
                if (jarPath.exists()) {
                    Files.copy(jarPath.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }

                // Replace with new JAR
                Files.move(tempFile.toPath(), jarPath.toPath(), StandardCopyOption.REPLACE_EXISTING)

                logger.info("Update completed successfully! Updated to version ${versionInfo.latestVersion}")
                println("\n✓ Kinfra has been updated to version ${versionInfo.latestVersion}")
                println("  Please restart kinfra to use the new version\n")

                true
            } else {
                logger.warn("Failed to download update: HTTP ${connection.responseCode}")
                false
            }
        } catch (e: Exception) {
            logger.error("Error during update: ${e.message}")
            false
        }
    }

    override fun getLastCheckTime(): Long {
        return try {
            if (lastCheckFile.exists()) {
                lastCheckFile.readText().toLongOrNull() ?: 0L
            } else {
                0L
            }
        } catch (e: Exception) {
            logger.warn("Error reading last check time: ${e.message}")
            0L
        }
    }

    override fun updateLastCheckTime() {
        try {
            appDir.mkdirs()
            lastCheckFile.writeText(System.currentTimeMillis().toString())
        } catch (e: Exception) {
            logger.warn("Error updating last check time: ${e.message}")
        }
    }
}