package net.kigawa.iac.cli

import net.kigawa.iac.model.KigawaNet
import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.ctx.NormalContext
import net.kigawa.kinfra.api.deploy.DeployGroupDepScope
import net.kigawa.kinfra.api.fs.DirPathResource
import net.kigawa.kinfra.api.fs.DirResource
import net.kigawa.kinfra.api.fs.FilePathResource
import net.kigawa.kinfra.infra.CliUserInterface
import net.kigawa.kinfra.infra.NormalDeployer
import net.kigawa.kinfra.infra.Xxh3Hasher
import net.kigawa.kinfra.infra.cmd.LocalCmdExecutor
import net.kigawa.kinfra.infra.file.LocalFileSystem
import net.kigawa.kinfra.infra.logging.ConsoleLogger
import net.kigawa.kinfra.infra.secret.BitwardenService
import net.kigawa.kinfra.infra.secret.FileSecret
import net.kigawa.kodel.api.dep.Dep
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.DepsBase
import java.util.logging.Logger

class IacCliDeps(depContext: DepContext<IacCliDepsScope>): DepsBase<IacCliDepsScope>(depContext) {
    val logger = dep {
        ConsoleLogger(Logger.getLogger(""))
    }
    val localCmdExecutor = dep {
        LocalCmdExecutor()
    }
    val secretDir = dep {
        DirResource(DirPathResource("~/.kinfra/secrets"))
    }
    val bitwardenSecretPath = dep {
        FilePathResource("~/.kinfra/bitwarden.secret")
    }
    val Xxh3Hasher = dep {
        Xxh3Hasher()
    }
    val localFileSystem = dep {
        LocalFileSystem()
    }
    val kinfraContext: Dep<NormalContext, IacCliDepsScope> = dep {
        KinfraContext.create(
            r2Deps.i().r2Recorder.i(),
            Xxh3Hasher.i(),
            localCmdExecutor.i(),
            localFileSystem.i(),
            logger.i(),
            CliUserInterface(),
            NormalDeployer(r2Deps.i().r2Recorder.i())
        )
    }
    val bitwardenSecretFile = dep {
        FileSecret(bitwardenSecretPath.i(), kinfraContext.i())
    }
    val bitwarden = dep {
        BitwardenService(
            localCmdExecutor.i(),
            bitwardenSecretFile.i().readOrType("bitwarden secret key"),
            logger.i(),
            kinfraContext.i(),
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
            "kigawa-bet", bitwarden.i(),
            childContext { DeployGroupDepScope(it, kinfraContext.i()) })
    }

    suspend fun main() = useDep {
        kinfraContext.i().let {
            it.deployer.deploy(kigawaNet.i(), it)
        }
    }
}