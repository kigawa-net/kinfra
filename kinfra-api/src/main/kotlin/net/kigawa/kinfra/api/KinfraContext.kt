package net.kigawa.kinfra.api

import net.kigawa.kinfra.api.process.CmdExecutor
import net.kigawa.kinfra.api.deploy.Deployer
import net.kigawa.kinfra.api.io.FileSystem
import net.kigawa.kinfra.model.logging.Logger

interface KinfraContext {
    val deployer: Deployer
    val cmdExecutor: CmdExecutor
    val fileSystem: FileSystem
    val logger: Logger
    fun childContext(): KinfraContext
}