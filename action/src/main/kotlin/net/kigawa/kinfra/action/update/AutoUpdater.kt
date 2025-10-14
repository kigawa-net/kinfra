package net.kigawa.kinfra.action.update

interface AutoUpdater {
    fun performUpdate(versionInfo: VersionInfo): Boolean
    fun getLastCheckTime(): Long
    fun updateLastCheckTime()
}
