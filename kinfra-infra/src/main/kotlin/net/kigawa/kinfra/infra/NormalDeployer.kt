package net.kigawa.kinfra.infra

import net.kigawa.kinfra.api.DeployRecorder
import net.kigawa.kinfra.api.deploy.Deployer
import net.kigawa.kinfra.api.hash.Hasher

class NormalDeployer(
    override val deployRecorder: DeployRecorder,
): Deployer {
    override fun createHasher(): Hasher {
        return Xxh3Hasher()
    }
}