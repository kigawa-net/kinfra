package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.cmd.CmdExecutor

interface DeployContext {
    val deployer: Deployer
    val cmdExecutor: CmdExecutor
}