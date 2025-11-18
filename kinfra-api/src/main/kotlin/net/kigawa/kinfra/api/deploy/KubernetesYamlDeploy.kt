package net.kigawa.kinfra.api.deploy

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.KinfraContext
import net.kigawa.kinfra.api.process.ProcessConfig
import net.kigawa.kinfra.api.process.StrCmd
import net.kigawa.kinfra.api.resource.YamlResource
import net.kigawa.kinfra.model.logging.Logger

class KubernetesYamlDeploy(
    val yamlResource: YamlResource,
    val logger: Logger
): KinfraDeploy {
    override suspend fun hash(hasher: Hasher): HashValue {
        return yamlResource.hash(hasher)
    }

    override suspend fun execute(ctx: KinfraContext) {
        ctx.cmdExecutor.execute(
            ProcessConfig.create(StrCmd(listOf("kubectl", "apply", "-f", "-")))
                .stdin { write(yamlResource.raw) }
                .stderr { forEach { logger.error(it) } }
        )
    }
}