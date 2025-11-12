package net.kigawa.kinfra.api

import net.kigawa.kinfra.api.cmd.CmdExecutor
import net.kigawa.kinfra.api.deploy.Deployer

interface KinfraContext {
    val deployer: Deployer
    val cmdExecutor: CmdExecutor
}