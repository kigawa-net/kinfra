package net.kigawa.kinfra.action.update

data class VersionInfo(
    val currentVersion: String,
    val latestVersion: String,
    val updateAvailable: Boolean,
    val downloadUrl: String = ""
)

interface VersionChecker {
    fun checkForUpdates(currentVersion: String, githubRepo: String): VersionInfo
    fun shouldCheckForUpdate(lastCheckTime: Long, checkInterval: Long): Boolean
}
