package net.kigawa.kinfra.infra.config.script

import net.kigawa.kinfra.infra.config.BitwardenSettingsScheme
import net.kigawa.kinfra.infra.config.KinfraConfigScheme
import net.kigawa.kinfra.infra.config.ProjectInfoScheme
import net.kigawa.kinfra.infra.config.TerraformSettingsScheme
import net.kigawa.kinfra.infra.config.UpdateSettingsScheme
import net.kigawa.kinfra.model.conf.BwsMarker
import kotlin.script.experimental.annotations.KotlinScript

/**
 * kinfra.ktsのDSL基底クラス（プロジェクト単位のTerraform設定）。
 *
 * 例:
 * ```
 * projectId = "abc123"
 * terraform {
 *     workingDirectory = "."
 *     variable("cloudflare_api_token", "cf-token")
 * }
 * ```
 */
@KotlinScript(
    displayName = "Kinfra Config",
    fileExtension = "kinfra.kts",
    compilationConfiguration = KinfraScriptCompilationConfiguration::class,
)
abstract class KinfraConfigScript {
    var projectId: String = ""
    var description: String? = null

    private var terraformScheme: TerraformSettingsScheme? = null
    private var bitwardenScheme: BitwardenSettingsScheme? = null
    private var updateScheme: UpdateSettingsScheme? = null

    fun terraform(block: TerraformConfigBuilder.() -> Unit) {
        terraformScheme = TerraformConfigBuilder().apply(block).build()
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

    internal fun toScheme(): KinfraConfigScheme =
        KinfraConfigScheme(
            project =
                ProjectInfoScheme(
                    projectIdField = projectId,
                    description = description,
                    terraform = terraformScheme,
                ),
            bitwarden = bitwardenScheme,
            update = updateScheme,
        )
}
