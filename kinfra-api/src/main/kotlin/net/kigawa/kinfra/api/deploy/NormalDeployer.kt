package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.DeployRecorder
import net.kigawa.kinfra.api.Hasher

class NormalDeployer(
    override val deployRecorder: DeployRecorder,
    override val hasher: Hasher,
): Deployer {
}