package net.kigawa.kinfra.api.ctx

import net.kigawa.kinfra.api.DeployRecorder
import net.kigawa.kinfra.api.UserInterface
import net.kigawa.kinfra.api.deploy.Deployer

import net.kigawa.kinfra.api.fs.FileSystem
import net.kigawa.kinfra.api.hash.Hasher
import net.kigawa.kinfra.api.process.CmdExecutor
import net.kigawa.kodel.api.log.Kogger

interface KinfraContext {
    companion object {
        fun create(
            deployRecorder: DeployRecorder, hasher: Hasher,
            cmdExecutor: CmdExecutor, fileSystem: FileSystem, kogger: Kogger, userInterface: UserInterface,
            deployer: Deployer,
        ) = NormalContext(
            deployer,
            cmdExecutor,
            fileSystem,
            kogger,
            emptyList(), userInterface
        )
    }

    val keys: List<String>
    val deployer: Deployer
    val cmdExecutor: CmdExecutor
    val fileSystem: FileSystem
    val kogger: Kogger
    val userInterface: UserInterface
    fun childContext(key: String): KinfraContext
}