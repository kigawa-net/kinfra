package net.kigawa.kinfra.infra.config.script

import net.kigawa.kinfra.infra.config.BitwardenSettingsScheme
import net.kigawa.kinfra.infra.config.SubProjectScheme
import net.kigawa.kinfra.infra.config.TerraformOutputMappingScheme
import net.kigawa.kinfra.infra.config.TerraformSettingsScheme
import net.kigawa.kinfra.infra.config.TerraformVariableMappingScheme
import net.kigawa.kinfra.infra.config.UpdateSettingsScheme

/**
 * kinfra.kts/kinfra-parent.ktsのDSLビルダー群。
 * `terraform { backendConfig { bucket = "..." } }`のようなネストしたブロック構文を
 * 構築するための素朴なミュータブルビルダー。
 */
class TerraformConfigBuilder {
    var version: String = ""
    var workingDirectory: String = "."
    var generateOutputDir: String? = null

    /**
     * サブプロジェクト変更検出のハッシュキャッシュ用R2バケット名/エンドポイント。
     * 各モジュールの`.tf`ファイルのbackendブロックに既にbucket/endpointがハードコードされている場合、
     * backendConfig{}に同じキーを含めるとterraform initが二重定義エラーになるため、
     * ここでは意図的にbackendConfigとは別のトップレベルフィールドとして扱う。
     */
    var r2Bucket: String? = null
    var r2Endpoint: String? = null
    private val backendConfigMap = mutableMapOf<String, String>()
    private val variableMappingsList = mutableListOf<TerraformVariableMappingScheme>()
    private val outputMappingsList = mutableListOf<TerraformOutputMappingScheme>()

    fun backendConfig(block: BackendConfigBuilder.() -> Unit) {
        backendConfigMap.putAll(BackendConfigBuilder().apply(block).build())
    }

    fun variable(
        terraformVariable: String,
        bitwardenSecretKey: String,
    ) {
        variableMappingsList += TerraformVariableMappingScheme(terraformVariable, bitwardenSecretKey)
    }

    fun output(
        terraformOutput: String,
        bitwardenSecretKey: String,
    ) {
        outputMappingsList += TerraformOutputMappingScheme(terraformOutput, bitwardenSecretKey)
    }

    fun build(): TerraformSettingsScheme =
        TerraformSettingsScheme(
            version = version,
            workingDirectory = workingDirectory,
            variableMappings = variableMappingsList.toList(),
            outputMappings = outputMappingsList.toList(),
            backendConfig = backendConfigMap.toMap(),
            generateOutputDir = generateOutputDir,
            r2Bucket = r2Bucket,
            r2Endpoint = r2Endpoint,
        )
}

class BackendConfigBuilder {
    var bucket: String? = null
    var key: String? = null
    var region: String? = null
    var endpoint: String? = null
    var accessKey: String? = null
    var secretKey: String? = null
    private val extra = mutableMapOf<String, String>()

    /**
     * bucket/key/region/endpoint/accessKey/secretKey以外の任意のbackend-configキーを追加する。
     */
    fun set(
        key: String,
        value: String,
    ) {
        extra[key] = value
    }

    fun build(): Map<String, String> =
        buildMap {
            bucket?.let { put("bucket", it) }
            key?.let { put("key", it) }
            region?.let { put("region", it) }
            endpoint?.let { put("endpoint", it) }
            accessKey?.let { put("access_key", it) }
            secretKey?.let { put("secret_key", it) }
            putAll(extra)
        }
}

class BitwardenConfigBuilder {
    var projectId: String = ""

    fun build(): BitwardenSettingsScheme = BitwardenSettingsScheme(projectId = projectId)
}

class UpdateConfigBuilder {
    var autoUpdate: Boolean = true
    var checkInterval: Long = 86400000
    var githubRepo: String = "kigawa-net/kinfra"

    fun build(): UpdateSettingsScheme =
        UpdateSettingsScheme(
            autoUpdate = autoUpdate,
            checkInterval = checkInterval,
            githubRepo = githubRepo,
        )
}

class SubProjectsBuilder {
    private val list = mutableListOf<SubProjectScheme>()

    fun subProject(
        name: String,
        path: String = name,
    ) {
        list += SubProjectScheme(name = name, path = path)
    }

    fun build(): List<SubProjectScheme> = list.toList()
}
