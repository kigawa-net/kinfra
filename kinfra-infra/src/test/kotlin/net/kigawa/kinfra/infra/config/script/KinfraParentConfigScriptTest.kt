package net.kigawa.kinfra.infra.config.script

import net.kigawa.kinfra.model.conf.BwsMarker
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class KinfraParentConfigScriptTest {
    @Test
    fun evaluatesRealShapedScript() {
        val content =
            """
            projectName = "kigawa-infra"
            description = "Parent project for managing multiple infrastructure components"

            terraform {
                backendConfig {
                    bucket = "kinfra"
                    key = "kinfra.tfstate"
                    region = "auto"
                    endpoint = bws("r2-api")
                    accessKey = bws("r2-access")
                    secretKey = bws("r2-secret")
                }
            }

            subProjects {
                subProject("host1")
                subProject("k8s", path = "kubernetes")
            }
            """.trimIndent()

        val file = createTempFile(suffix = ".kinfra-parent.kts").toFile()
        file.writeText(content)
        try {
            val script = ScriptHost.eval<KinfraParentConfigScript>(file)
            val scheme = script.toScheme()

            assertEquals("kigawa-infra", scheme.projectName)
            assertEquals("Parent project for managing multiple infrastructure components", scheme.description)

            val backendConfig = scheme.terraform?.backendConfig
            assertNotNull(backendConfig)
            assertEquals("kinfra", backendConfig["bucket"])
            assertEquals("kinfra.tfstate", backendConfig["key"])
            assertEquals("auto", backendConfig["region"])
            assertEquals(BwsMarker.wrap("r2-api"), backendConfig["endpoint"])
            assertEquals(BwsMarker.wrap("r2-access"), backendConfig["access_key"])
            assertEquals(BwsMarker.wrap("r2-secret"), backendConfig["secret_key"])

            assertEquals(2, scheme.subProjects.size)
            assertEquals("host1", scheme.subProjects[0].name)
            assertEquals("host1", scheme.subProjects[0].path)
            assertEquals("k8s", scheme.subProjects[1].name)
            assertEquals("kubernetes", scheme.subProjects[1].path)
        } finally {
            file.delete()
        }
    }

    @Test
    fun defaultsAreEmptyWhenNoBlocksAreUsed() {
        val file = createTempFile(suffix = ".kinfra-parent.kts").toFile()
        file.writeText("projectName = \"minimal\"")
        try {
            val script = ScriptHost.eval<KinfraParentConfigScript>(file)
            val scheme = script.toScheme()

            assertEquals("minimal", scheme.projectName)
            assertNull(scheme.description)
            assertNull(scheme.terraform)
            assertEquals(emptyList(), scheme.subProjects)
        } finally {
            file.delete()
        }
    }
}
