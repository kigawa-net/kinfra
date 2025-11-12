package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.cmd.StrCmd
import net.kigawa.kinfra.api.deploy.KinfraContext

class KubernetesYamlDeploy(
    val yamlResource: YamlResource,
): KinfraDeploy {
    override fun hash(hasher: Hasher, ctx: KinfraContext): HashValue {
        return yamlResource.hash(hasher, ctx)
    }

    override suspend fun execute(ctx: KinfraContext) {
        val res = ctx.cmdExecutor.execute(StrCmd("kubectl", "apply", "-f", "-"))
        res.writer {
            it.write(yamlResource.raw)
        }
    }
}