package net.kigawa.iac.cli

import net.kigawa.kinfra.infra.r2.R2DeployRecorder
import net.kigawa.kinfra.infra.secret.BitwardenService
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.DepsBase

class R2Deps(
    depContext: DepContext<IacCliDepsScope>,
    val bitwarden: BitwardenService,
): DepsBase<IacCliDepsScope>(depContext) {
    val accountId = dep {
        bitwarden.getSecret("9a87417e-e90e-41db-89e8-b37000ecd720")
    }
    val accessKey = dep {
        bitwarden.getSecret("eb5eb0e8-2a4a-4398-a756-b37000d87d64")
    }
    val secretKey = dep {
        bitwarden.getSecret("c39086cc-e112-40eb-b19f-b37000d89090")
    }
    val bucketName = dep {
        bitwarden.getSecret("d5ab65a6-6ff9-4153-8673-b37000eacc98")
    }

    val r2Recorder = dep {
        R2DeployRecorder(
            accountId.i().value,
            accessKey.i().value,
            secretKey.i().value,
            bucketName.i().value,
        )
    }
}