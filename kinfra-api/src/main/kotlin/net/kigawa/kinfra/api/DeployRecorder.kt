package net.kigawa.kinfra.api

interface DeployRecorder {
    suspend fun record(hash: HashValue, block: suspend () -> Unit)
}