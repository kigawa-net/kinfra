package net.kigawa.kinfra.api.process

interface CmdExecutor {
    suspend fun <SI, SO, SE> execute(processConfig: ProcessConfig<SI, SO, SE>): ProcessRes<SI, SO, SE>
}