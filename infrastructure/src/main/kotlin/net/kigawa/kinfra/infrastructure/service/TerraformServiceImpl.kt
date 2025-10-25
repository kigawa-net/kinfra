package net.kigawa.kinfra.infrastructure.service

import net.kigawa.kinfra.infrastructure.process.ProcessExecutor
import net.kigawa.kinfra.infrastructure.terraform.TerraformRepository
import net.kigawa.kinfra.model.conf.TerraformConfig
import net.kigawa.kinfra.model.err.ActionException
import net.kigawa.kinfra.model.err.Res
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.action.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.action.bitwarden.BitwardenRepository
import net.kigawa.kinfra.action.config.ConfigRepository
import java.io.File
import java.nio.file.Paths

/**
 * TerraformServiceの実装
 */
class TerraformServiceImpl(
    private val processExecutor: ProcessExecutor,
    private val terraformRepository: TerraformRepository,
    private val configRepository: ConfigRepository,
    private val bitwardenSecretManagerRepository: BitwardenSecretManagerRepository? = null,
    private val bitwardenRepository: BitwardenRepository? = null,
): TerraformService {

    override fun init(additionalArgs: List<String>, quiet: Boolean): Res<Int, ActionException> {
        val config = terraformRepository.getTerraformConfig()
        if (config == null) {
            return Res.Err(ActionException(1, "Terraform configuration not found"))
        }

        val args = mutableListOf("terraform", "init", "-input=false")

        // backendConfigから-backend-configオプションを追加
        config.backendConfig.forEach { (key, value) ->
            args.add("-backend-config=$key=$value")
        }

        args.addAll(additionalArgs)

        return processExecutor.execute(
            args = args.toTypedArray(),
            workingDir = config.workingDirectory,
            environment = mapOf("SSH_CONFIG" to config.sshConfigPath),
            quiet = quiet
        )
    }

    override fun plan(additionalArgs: List<String>, quiet: Boolean): Res<Int, ActionException> {
        val config = terraformRepository.getTerraformConfig()
        if (config == null) {
            return Res.Err(ActionException(1, "Terraform configuration not found"))
        }

        // Bitwardenシークレットから.tfvarsファイルを生成
        val generatedTfvarsFile = generateTfvarsFromBitwarden()?.let { content ->
            saveTfvarsFile(config, content)
        }

        val varFileArgs = mutableListOf<String>()

        // 既存のvarFileがある場合
        if (config.hasVarFile()) {
            varFileArgs.add("-var-file=${config.varFile!!.absolutePath}")
        }

        // 生成された.tfvarsファイルがある場合
        if (generatedTfvarsFile != null) {
            varFileArgs.add("-var-file=${generatedTfvarsFile.absolutePath}")
        }

        val args = mutableListOf("terraform", "plan", "-input=false")

        // backendConfigから-backend-configオプションを追加
        config.backendConfig.forEach { (key, value) ->
            args.add("-backend-config=$key=$value")
        }

        args.addAll(varFileArgs)
        args.addAll(additionalArgs)

        return processExecutor.execute(
            args = args.toTypedArray(),
            workingDir = config.workingDirectory,
            environment = mapOf("SSH_CONFIG" to config.sshConfigPath),
            quiet = quiet
        )
    }

    override fun apply(
        planFile: String?, additionalArgs: List<String>, quiet: Boolean,
    ): Res<Int, ActionException> {
        val config = terraformRepository.getTerraformConfig()
        if (config == null) {
            return Res.Err(ActionException(1, "Terraform configuration not found"))
        }

        // Bitwardenシークレットから.tfvarsファイルを生成
        val generatedTfvarsFile = generateTfvarsFromBitwarden()?.let { content ->
            saveTfvarsFile(config, content)
        }

        val baseArgs = mutableListOf("terraform", "apply")
        val varFileArgs = mutableListOf<String>()

        // 既存のvarFileがある場合（planFileがない場合のみ）
        if (planFile == null && config.hasVarFile()) {
            varFileArgs.add("-var-file=${config.varFile!!.absolutePath}")
        }

        // 生成された.tfvarsファイルがある場合
        if (generatedTfvarsFile != null) {
            varFileArgs.add("-var-file=${generatedTfvarsFile.absolutePath}")
        }

        val planArgs = if (planFile != null) listOf(planFile) else emptyList()

        // backendConfigから-backend-configオプションで追加
        config.backendConfig.forEach { (key, value) ->
            baseArgs.add("-backend-config=$key=$value")
        }

        val args = baseArgs + listOf("-input=false") + additionalArgs + varFileArgs + planArgs

        return processExecutor.execute(
            args = args.toTypedArray(),
            workingDir = config.workingDirectory,
            environment = mapOf("SSH_CONFIG" to config.sshConfigPath),
            quiet = quiet
        )
    }

    override fun destroy(additionalArgs: List<String>, quiet: Boolean): Res<Int, ActionException> {
        val config = terraformRepository.getTerraformConfig()
        if (config == null) {
            return Res.Err(ActionException(1, "Terraform configuration not found"))
        }

        val varFileArgs = if (config.hasVarFile()) {
            arrayOf("-var-file=${config.varFile!!.absolutePath}")
        } else {
            emptyArray()
        }

        val args = arrayOf("terraform", "destroy") + varFileArgs + additionalArgs

        return processExecutor.execute(
            args = args,
            workingDir = config.workingDirectory,
            environment = mapOf("SSH_CONFIG" to config.sshConfigPath),
            quiet = quiet
        )
    }

    override fun format(recursive: Boolean, quiet: Boolean): Res<Int, ActionException> {
        val args = if (recursive) {
            arrayOf("terraform", "fmt", "-recursive")
        } else {
            arrayOf("terraform", "fmt")
        }

        return processExecutor.execute(args = args, quiet = quiet)
    }

    override fun validate(quiet: Boolean): Res<Int, ActionException> {
        return processExecutor.execute(args = arrayOf("terraform", "validate"), quiet = quiet)
    }

    override fun show(additionalArgs: List<String>, quiet: Boolean): Res<Int, ActionException> {
        val config = terraformRepository.getTerraformConfig()
        if (config == null) {
            return Res.Err(ActionException(1, "Terraform configuration not found"))
        }

        val args = arrayOf("terraform", "show") + additionalArgs

        return processExecutor.execute(
            args = args,
            workingDir = config.workingDirectory,
            environment = mapOf("SSH_CONFIG" to config.sshConfigPath),
            quiet = quiet
        )
    }

    override fun getTerraformConfig(): TerraformConfig? {
        return terraformRepository.getTerraformConfig()
    }

    /**
     * Bitwardenシークレットから.tfvarsファイルを生成
     */
    private fun generateTfvarsFromBitwarden(): String? {
        val configPath = configRepository.getProjectConfigFilePath()
        val kinfraConfig = configRepository.loadKinfraConfig(Paths.get(configPath)) ?: return null

        val settings = kinfraConfig.rootProject.terraform ?: return null
        if (settings.variableMappings.isEmpty() || bitwardenSecretManagerRepository == null) {
            return null
        }

        val tfvarsContent = StringBuilder()
        for (mapping in settings.variableMappings) {
            val secret = bitwardenSecretManagerRepository.findSecretByKey(mapping.bitwardenSecretKey)
            if (secret != null) {
                tfvarsContent.append("${mapping.terraformVariable} = \"${secret.value}\"\n")
            }
        }

        return if (tfvarsContent.isNotEmpty()) tfvarsContent.toString() else null
    }

    /**
     * Bitwardenシークレットからbackend.tfvarsファイルを生成
     */
    private fun generateBackendTfvarsFromBitwarden(): String? {
        println("DEBUG: Attempting to generate backend.tfvars from Bitwarden")
        if (bitwardenRepository == null) {
            println("DEBUG: BitwardenRepository is null")
            return null
        }

        // BitwardenからR2バックエンド設定を取得
        if (!bitwardenRepository.isLoggedIn()) {
            println("DEBUG: Not logged in to Bitwarden")
            return null
        }

        val session = bitwardenRepository.getSessionFromFile()
            ?: bitwardenRepository.getSessionFromEnv()
        if (session == null) {
            println("DEBUG: No Bitwarden session found")
            return null
        }

        val item = bitwardenRepository.getItem("Cloudflare R2 Terraform Backend", session)
        if (item == null) {
            println("DEBUG: Bitwarden item 'Cloudflare R2 Terraform Backend' not found")
            return null
        }

        val accessKey = item.getFieldValue("access_key")
        val secretKey = item.getFieldValue("secret_key")
        val accountId = item.getFieldValue("account_id")
        val bucketName = item.getFieldValue("bucket_name") ?: "kigawa-infra-state"

        if (accessKey == null || secretKey == null || accountId == null) {
            println("DEBUG: Missing required fields in Bitwarden item: access_key=$accessKey, secret_key=${secretKey != null}, account_id=$accountId")
            return null
        }

        println("DEBUG: Successfully retrieved backend config from Bitwarden")
        val config = net.kigawa.kinfra.model.conf.R2BackendConfig(
            bucket = bucketName,
            key = "terraform.tfstate",
            endpoint = "https://$accountId.r2.cloudflarestorage.com",
            accessKey = accessKey,
            secretKey = secretKey
        )

        return config.toTfvarsContent()
    }

    /**
     * .tfvarsファイルを保存
     */
    private fun saveTfvarsFile(config: TerraformConfig, content: String): File {
        val tfvarsFile = File(config.workingDirectory, "secrets.tfvars")
        tfvarsFile.writeText(content)
        return tfvarsFile
    }

    /**
     * backend.tfvarsファイルを保存
     */
    private fun saveBackendTfvarsFile(config: TerraformConfig, content: String): File {
        val backendTfvarsFile = File(config.workingDirectory, "backend.tfvars")
        backendTfvarsFile.writeText(content)
        println("DEBUG: Created backend.tfvars file at ${backendTfvarsFile.absolutePath}")
        return backendTfvarsFile
    }
}