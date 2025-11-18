package net.kigawa.kinfra.api.process

interface CmdExecutor {
    fun <SI, SO,SE> execute(processConfig: ProcessConfig<SI, SO,SE>): ProcessRes<SI, SO,SE>
}