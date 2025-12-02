package net.kigawa.kinfra.infra.update

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.kigawa.kodel.api.log.Kogger
import net.kigawa.kinfra.model.update.VersionChecker
import net.kigawa.kinfra.model.update.VersionInfo
import net.kigawa.kodel.api.log.traceignore.debug
import net.kigawa.kodel.api.log.traceignore.warn
import java.net.HttpURLConnection
import java.net.URL

class VersionCheckerImpl(
    private val kogger: Kogger,
) : VersionChecker {
    private val gson = Gson()

    override fun checkForUpdates(
        currentVersion: String,
        githubRepo: String,
    ): VersionInfo {
        return try {
            kogger.debug("Checking for updates from GitHub repository: $githubRepo")

            val apiUrl = "https://api.github.com/repos/$githubRepo/releases/latest"
            val connection = URL(apiUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = gson.fromJson(response, JsonObject::class.java)

                val latestVersion = jsonObject.get("tag_name")?.asString?.removePrefix("v") ?: currentVersion
                val updateAvailable = compareVersions(currentVersion, latestVersion) < 0

                val downloadUrl =
                    if (updateAvailable) {
                        "https://github.com/$githubRepo/releases/download/v$latestVersion/kinfra-cli-$latestVersion.jar"
                    } else {
                        ""
                    }

                kogger.debug("Current version: $currentVersion, Latest version: $latestVersion, Update available: $updateAvailable")

                VersionInfo(
                    currentVersion = currentVersion,
                    latestVersion = latestVersion,
                    updateAvailable = updateAvailable,
                    downloadUrl = downloadUrl,
                )
            } else {
                kogger.warn("Failed to check for updates: HTTP ${connection.responseCode}")
                VersionInfo(currentVersion, currentVersion, false)
            }
        } catch (e: Exception) {
            kogger.warn("Error checking for updates: ${e.message}")
            VersionInfo(currentVersion, currentVersion, false)
        }
    }

    override fun shouldCheckForUpdate(
        lastCheckTime: Long,
        checkInterval: Long,
    ): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastCheckTime) >= checkInterval
    }

    private fun compareVersions(
        current: String,
        latest: String,
    ): Int {
        // Skip comparison for dev versions
        if (current == "dev" || latest == "dev") return 0

        try {
            val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
            val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }

            val maxLength = maxOf(currentParts.size, latestParts.size)

            for (i in 0 until maxLength) {
                val currentPart = currentParts.getOrNull(i) ?: 0
                val latestPart = latestParts.getOrNull(i) ?: 0

                when {
                    currentPart < latestPart -> return -1
                    currentPart > latestPart -> return 1
                }
            }

            return 0
        } catch (e: Exception) {
            kogger.warn("Error comparing versions: ${e.message}")
            return 0
        }
    }
}
