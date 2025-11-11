package net.kigawa.kinfra.api.cmd

interface CmdExecutor {
    fun execute(cmd: Cmd): CmdRes
}