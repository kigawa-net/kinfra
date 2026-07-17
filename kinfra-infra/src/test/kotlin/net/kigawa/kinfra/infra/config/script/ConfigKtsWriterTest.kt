package net.kigawa.kinfra.infra.config.script

import net.kigawa.kinfra.infra.config.KinfraParentConfigScheme
import net.kigawa.kinfra.infra.config.SubProjectScheme
import net.kigawa.kinfra.infra.config.TerraformSettingsScheme
import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigKtsWriterTest {
    @Test
    fun renderedKtsRoundTripsThroughScriptEval() {
        val original =
            KinfraParentConfigScheme(
                projectName = "kigawa-infra",
                description = "Parent project for managing multiple infrastructure components",
                terraform =
                    TerraformSettingsScheme(
                        workingDirectory = ".",
                        backendConfig =
                            mapOf(
                                "bucket" to "kinfra",
                                "key" to "kinfra.tfstate",
                                "region" to "auto",
                                "endpoint" to BwsMarker.wrap("r2-api"),
                                "access_key" to BwsMarker.wrap("r2-access"),
                                "secret_key" to BwsMarker.wrap("r2-secret"),
                            ),
                    ),
                subProjects =
                    listOf(
                        SubProjectScheme(name = "host1"),
                        SubProjectScheme(name = "k8s", path = "kubernetes"),
                    ),
            )

        val rendered = KinfraParentConfigKtsWriter.render(original)

        val file = createTempFile(suffix = ".kinfra-parent.kts").toFile()
        file.writeText(rendered)
        try {
            val roundTripped = ScriptHost.eval<KinfraParentConfigScript>(file).toScheme()
            assertEquals(original, roundTripped)
        } finally {
            file.delete()
        }
    }
}
