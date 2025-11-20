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
        bitwarden.getSecret("r2-account")
    }
    val accessKey = dep {
        bitwarden.getSecret("r2-access")
    }
    val secretKey = dep {
        bitwarden.getSecret("r2-secret")
    }
    val bucketName = dep {
        bitwarden.getSecret("r2-bucket")
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