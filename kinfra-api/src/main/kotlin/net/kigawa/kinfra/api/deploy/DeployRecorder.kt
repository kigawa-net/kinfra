package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.HashValue

interface DeployRecorder {
    suspend fun record(hash: HashValue, block: suspend () -> Unit)
}