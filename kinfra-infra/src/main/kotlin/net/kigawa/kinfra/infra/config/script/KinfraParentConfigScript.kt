package net.kigawa.kinfra.infra.config.script

import net.kigawa.kinfra.infra.config.BitwardenSettingsScheme
import net.kigawa.kinfra.infra.config.KinfraParentConfigScheme
import net.kigawa.kinfra.infra.config.SubProjectScheme
import net.kigawa.kinfra.infra.config.TerraformSettingsScheme
import net.kigawa.kinfra.infra.config.UpdateSettingsScheme
import net.kigawa.kinfra.model.conf.BwsMarker
import kotlin.script.experimental.annotations.KotlinScript

/**
 * kinfra-parent.ktsのDSL基底クラス。
 *
 * 例:
 * ```
 * projectName = "kigawa-infra"
 * terraform {
 *     backendConfig {
 *         bucket = "kinfra"
 *         endpoint = bws("r2-api")
 *     }
 * }
 * subProjects {
 *     subProject("host1")
 *     subProject("k8s", path = "kubernetes")
 * }
 * ```
 */
@KotlinScript(
    displayName = "Kinfra Parent Config",
    fileExtension = "kinfra-parent.kts",
    compilationConfiguration = KinfraScriptCompilationConfiguration::class,
)
abstract class KinfraParentConfigScript {
    var projectName: String = ""
    var description: String? = null

    private var terraformScheme: TerraformSettingsScheme? = null
    private val subProjectSchemes = mutableListOf<SubProjectScheme>()
    private var bitwardenScheme: BitwardenSettingsScheme? = null
    private var updateScheme: UpdateSettingsScheme? = null

    fun terraform(block: TerraformConfigBuilder.() -> Unit) {
        terraformScheme = TerraformConfigBuilder().apply(block).build()
    }

    fun subProjects(block: SubProjectsBuilder.() -> Unit) {
        subProjectSchemes += SubProjectsBuilder().apply(block).build()
    }

    fun bitwarden(block: BitwardenConfigBuilder.() -> Unit) {
        bitwardenScheme = BitwardenConfigBuilder().apply(block).build()
    }

    fun update(block: UpdateConfigBuilder.() -> Unit) {
        updateScheme = UpdateConfigBuilder().apply(block).build()
    }

    /**
     * Bitwarden Secret Managerのシークレットを参照する。値はここでは解決されず、
     * 実際にterraformを呼び出す直前に解決される（BwsMarker参照）。
     */
    fun bws(secretKey: String): String = BwsMarker.wrap(secretKey)

    internal fun toScheme(): KinfraParentConfigScheme =
        KinfraParentConfigScheme(
            projectName = projectName,
            description = description,
            terraform = terraformScheme,
            subProjects = subProjectSchemes.toList(),
            bitwarden = bitwardenScheme,
            update = updateScheme,
        )
}
