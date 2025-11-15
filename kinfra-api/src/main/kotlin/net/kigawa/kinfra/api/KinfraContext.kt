package net.kigawa.kinfra.api

import net.kigawa.kinfra.api.cmd.CmdExecutor
import net.kigawa.kinfra.api.deploy.Deployer
import net.kigawa.kinfra.api.io.FileSystem

interface KinfraContext {
    val deployer: Deployer
    val cmdExecutor: CmdExecutor
    val fileSystem: FileSystem
    fun childContext(): KinfraContext
}