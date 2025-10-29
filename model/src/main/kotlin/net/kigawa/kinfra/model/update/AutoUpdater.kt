package net.kigawa.kinfra.model.update

interface AutoUpdater {
    fun performUpdate(versionInfo: VersionInfo): Boolean
    fun getLastCheckTime(): Long
    fun updateLastCheckTime()
}
