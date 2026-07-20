package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kinfra.api.process.ProcessConfig
import net.kigawa.kinfra.api.process.StrCmd
import net.kigawa.kinfra.api.resource.YamlResource
import net.kigawa.kodel.api.log.Kogger
import net.kigawa.kodel.api.log.traceignore.error

class KubernetesYamlDeploy(
    override val name: String,
    val yamlResource: YamlResource,
    val kogger: Kogger,
): KinfraDeploy {


    override suspend fun execute(ctx: KinfraContext) {
        ctx.cmdExecutor.execute(
            ProcessConfig.create(StrCmd(listOf("kubectl", "apply", "-f", "-")))
                .stdin { write(yamlResource.raw) }
                .stderr { forEach { kogger.error(it) } }
        )
    }

    override suspend fun hashSrc(): HashSrc {
        return HashSrc.resource(yamlResource)
    }
}