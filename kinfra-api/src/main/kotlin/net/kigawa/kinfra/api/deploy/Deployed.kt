package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.resource.KinfraDeploy

class Deployed<T: KinfraDeploy>(
    val deploy: T,
) {
}