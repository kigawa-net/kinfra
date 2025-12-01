package net.kigawa.iac.cli

import net.kigawa.iac.model.KigawaNet
import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.ctx.NormalContext
import net.kigawa.kinfra.api.deploy.DeployGroupDepScope
import net.kigawa.kinfra.infra.CliUserInterface
import net.kigawa.kinfra.infra.NormalDeployer
import net.kigawa.kinfra.infra.Xxh3Hasher
import net.kigawa.kinfra.infra.cmd.LocalCmdExecutor
import net.kigawa.kinfra.infra.file.LocalFileSystem
import net.kigawa.kinfra.infra.secret.BitwardenService
import net.kigawa.kinfra.infra.secret.FileSecret
import net.kigawa.kodel.api.dep.Dep
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.DepsBase
import net.kigawa.kodel.api.log.getLogger

class IacCliDeps(depContext: DepContext<IacCliDepsScope>): DepsBase<IacCliDepsScope>(depContext) {
    val logger = getLogger()

    val localFileSystem = LocalFileSystem()

    val localCmdExecutor = dep {
        LocalCmdExecutor(logger)
    }
    val kinfraDir = dep {
        localFileSystem.homeDir().createSubDir(".kinfra")
    }
    val secretDir = dep {
        kinfraDir.i().createSubDir("secrets")
    }
    val bitwardenSecretPath = dep {
        kinfraDir.i().childFilePath("bitwarden.secret")
    }
    val xxh3Hasher = dep {
        Xxh3Hasher()
    }
    val userInterface = dep {
        CliUserInterface()
    }
    val kinfraContext: Dep<NormalContext, IacCliDepsScope> = dep {
        KinfraContext.create(
            r2Deps.i().r2Recorder.i(),
            xxh3Hasher.i(),
            localCmdExecutor.i(),
            localFileSystem,
            logger,
            userInterface.i(),
            NormalDeployer(r2Deps.i().r2Recorder.i())
        )
    }
    val bitwardenSecretFile = dep {
        FileSecret(bitwardenSecretPath.i(), localFileSystem, userInterface.i())
    }
    val bitwarden: Dep<BitwardenService, IacCliDepsScope> = dep {
        BitwardenService(
            localCmdExecutor.i(),
            bitwardenSecretFile.i().readOrType("bitwarden secret key"),
            secretDir.i()
        )
    }
    val r2Deps = dep {
        R2Deps(
            childContext { it.newDepScope() },
            bitwarden.i(),
        )
    }
    val kigawaNet = dep {
        KigawaNet(
            "kigawa-net", bitwarden.i(),
            childContext { DeployGroupDepScope(it, kinfraContext.i()) })
    }

    suspend fun main() = useDep {
        kinfraContext.i().let {
            it.deployer.deploy(kigawaNet.i(), it)
        }
    }
}