package net.kigawa.kinfra.api.cmd

import net.kigawa.kinfra.api.io.Reader
import net.kigawa.kinfra.api.io.Writer

interface CmdRes {
    suspend fun writer(block: suspend (Writer) -> Unit)
    suspend fun reader(block: suspend (Reader) -> Unit)
}