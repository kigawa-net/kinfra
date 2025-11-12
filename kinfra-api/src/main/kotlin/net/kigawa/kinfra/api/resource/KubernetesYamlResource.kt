package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.Yaml
import net.kigawa.kinfra.api.cmd.StrCmd
import net.kigawa.kinfra.api.deploy.DeployContext

class KubernetesYamlResource(
    val yaml: Yaml,
): KinfraResource {
    override fun hashSrc(): String {
        return yaml.hashSrc()
    }

    override suspend fun execute(ctx: DeployContext) {
        val res = ctx.cmdExecutor.execute(StrCmd("kubectl", "apply", "-f", "-"))
        res.writer {
            it.write(yaml.raw)
        }
    }
}