package net.kigawa.iac.cli

import net.kigawa.iac.model.KigawaNet
import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.fs.FilePathResource
import net.kigawa.kinfra.infra.Xxh3Hasher
import net.kigawa.kinfra.infra.cmd.LocalCmdExecutor
import net.kigawa.kinfra.infra.file.LocalFileSystem
import net.kigawa.kinfra.infra.logging.ConsoleLogger
import net.kigawa.kinfra.infra.secret.BitwardenService
import net.kigawa.kinfra.infra.secret.FileSecret
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
    val bitwardenSecretPath = dep {
        FilePathResource("~/.kinfra/bitwarden.secret")
    }
    val Xxh3Hasher = dep {
        Xxh3Hasher()
    }
    val localFileSystem = dep {
        LocalFileSystem()
    }
    val kinfraContext = dep {
        KinfraContext.create(
            r2Deps.i().r2Recorder.i(),
            Xxh3Hasher.i(),
            localCmdExecutor.i(),
            localFileSystem.i(),
            logger.i(),
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
        )
    }
    val r2Deps = dep {
        R2Deps(
            childContext { it.newDepScope() },
            bitwarden.i(),
        )
    }
    val kigawaNet = dep {
        KigawaNet("kigawa-bet", kinfraContext.i(), bitwarden.i())
    }

    suspend fun main() = useDep {
        kinfraContext.i().let {
            it.deployer.deploy(kigawaNet.i(), it)
        }
    }
}