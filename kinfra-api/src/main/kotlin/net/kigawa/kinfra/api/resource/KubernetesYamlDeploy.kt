package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.cmd.StrCmd
import net.kigawa.kinfra.api.deploy.DeployContext

class KubernetesYamlDeploy(
    val yamlResource: YamlResource,
): KinfraDeploy {
    override fun hash(hasher: Hasher): HashValue {
        return yamlResource.hash(hasher)
    }

    override suspend fun execute(ctx: DeployContext) {
        val res = ctx.cmdExecutor.execute(StrCmd("kubectl", "apply", "-f", "-"))
        res.writer {
            it.write(yamlResource.raw)
        }
    }
}