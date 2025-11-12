package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.cmd.CmdExecutor

interface KinfraContext {
    val deployer: Deployer
    val cmdExecutor: CmdExecutor
}