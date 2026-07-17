package net.kigawa.kinfra.infra.config.script

import net.kigawa.kinfra.infra.config.KinfraConfigScheme
import net.kigawa.kinfra.infra.config.KinfraParentConfigScheme
import net.kigawa.kinfra.infra.config.TerraformSettingsScheme
import net.kigawa.kinfra.model.conf.BwsMarker

/**
 * KinfraParentConfigScheme/KinfraConfigSchemeオブジェクトから、正規化された
 * .ktsソーステキストを生成する。kamlのencodeToStringに相当する書き込み用プリンタ。
 *
 * 現状のYAML(kaml)ベースの保存も、既存のコメントや書式を保持せずファイル全体を
 * オブジェクトから再生成する挙動であり、これはそれと同じ制約を引き継ぐ
 * （手書きのKotlinロジックがあっても、sub add等のプログラム的な変更を行うと
 * 正規化された宣言的な内容で上書きされる）。
 */
object KinfraParentConfigKtsWriter {
    fun render(scheme: KinfraParentConfigScheme): String =
        buildString {
            appendLine("projectName = ${quoted(scheme.projectName)}")
            scheme.description?.let { appendLine("description = ${quoted(it)}") }

            renderTerraformBlock(scheme.terraform)?.let {
                appendLine()
                append(it)
            }

            if (scheme.subProjects.isNotEmpty()) {
                appendLine()
                appendLine("subProjects {")
                scheme.subProjects.forEach { sp ->
                    if (sp.path == sp.name) {
                        appendLine("    subProject(${quoted(sp.name)})")
                    } else {
                        appendLine("    subProject(${quoted(sp.name)}, path = ${quoted(sp.path)})")
                    }
                }
                appendLine("}")
            }

            scheme.bitwarden?.let { bw ->
                appendLine()
                appendLine("bitwarden {")
                appendLine("    projectId = ${quoted(bw.projectId)}")
                appendLine("}")
            }

            scheme.update?.let { u ->
                appendLine()
                appendLine("update {")
                appendLine("    autoUpdate = ${u.autoUpdate}")
                appendLine("    checkInterval = ${u.checkInterval}")
                appendLine("    githubRepo = ${quoted(u.githubRepo)}")
                appendLine("}")
            }
        }
}

/**
 * kinfra.kts（プロジェクト単位のTerraform設定）用のライター。
 */
object KinfraConfigKtsWriter {
    fun render(scheme: KinfraConfigScheme): String =
        buildString {
            appendLine("projectId = ${quoted(scheme.rootProject.projectId)}")
            scheme.rootProject.description?.let { appendLine("description = ${quoted(it)}") }

            renderTerraformBlock(scheme.rootProject.terraform)?.let {
                appendLine()
                append(it)
            }

            scheme.bitwarden?.let { bw ->
                appendLine()
                appendLine("bitwarden {")
                appendLine("    projectId = ${quoted(bw.projectId)}")
                appendLine("}")
            }

            scheme.update?.let { u ->
                appendLine()
                appendLine("update {")
                appendLine("    autoUpdate = ${u.autoUpdate}")
                appendLine("    checkInterval = ${u.checkInterval}")
                appendLine("    githubRepo = ${quoted(u.githubRepo)}")
                appendLine("}")
            }
        }
}

private fun renderTerraformBlock(tf: TerraformSettingsScheme?): String? {
    if (tf == null) return null
    return buildString {
        appendLine("terraform {")
        if (tf.version.isNotEmpty()) appendLine("    version = ${quoted(tf.version)}")
        appendLine("    workingDirectory = ${quoted(tf.workingDirectory)}")
        tf.generateOutputDir?.let { appendLine("    generateOutputDir = ${quoted(it)}") }
        tf.r2Bucket?.let { appendLine("    r2Bucket = ${renderBackendConfigValue(it)}") }
        tf.r2Endpoint?.let { appendLine("    r2Endpoint = ${renderBackendConfigValue(it)}") }

        if (tf.backendConfig.isNotEmpty()) {
            appendLine("    backendConfig {")
            tf.backendConfig.forEach { (key, value) ->
                appendLine("        ${renderBackendConfigEntry(key, value)}")
            }
            appendLine("    }")
        }

        tf.variableMappings.forEach {
            appendLine("    variable(${quoted(it.terraformVariable)}, ${quoted(it.bitwardenSecretKey)})")
        }
        tf.outputMappings.forEach {
            appendLine("    output(${quoted(it.terraformOutput)}, ${quoted(it.bitwardenSecretKey)})")
        }
        appendLine("}")
    }
}

private fun renderBackendConfigEntry(
    key: String,
    value: String,
): String {
    val propertyName =
        when (key) {
            "access_key" -> "accessKey"
            "secret_key" -> "secretKey"
            "bucket", "key", "region", "endpoint" -> key
            else -> null
        }

    val valueExpr = renderBackendConfigValue(value)

    return if (propertyName != null) {
        "$propertyName = $valueExpr"
    } else {
        "set(${quoted(key)}, $valueExpr)"
    }
}

private fun renderBackendConfigValue(value: String): String {
    val bwsKey = BwsMarker.extractKey(value)
    return if (bwsKey != null) "bws(${quoted(bwsKey)})" else quoted(value)
}

private fun quoted(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
