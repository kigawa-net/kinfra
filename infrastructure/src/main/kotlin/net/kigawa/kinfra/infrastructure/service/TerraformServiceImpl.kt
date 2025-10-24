package net.kigawa.kinfra.infrastructure.service

import net.kigawa.kinfra.infrastructure.process.ProcessExecutor
import net.kigawa.kinfra.infrastructure.terraform.TerraformRepository
import net.kigawa.kinfra.model.conf.TerraformConfig
import net.kigawa.kinfra.model.err.ActionException
import net.kigawa.kinfra.model.err.Res
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.action.bitwarden.BitwardenSecretManagerRepository
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
): TerraformService {

    override fun init(additionalArgs: List<String>, quiet: Boolean): Res<Int, ActionException> {
        val config = terraformRepository.getTerraformConfig()
        if (config == null) {
            return Res.Err(ActionException(1, "Terraform configuration not found"))
        }

        val args = arrayOf("terraform", "init") + additionalArgs

        return processExecutor.execute(
            args = args,
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

        val args = arrayOf("terraform", "plan") + varFileArgs + additionalArgs

        return processExecutor.execute(
            args = args,
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

        val baseArgs = arrayOf("terraform", "apply")
        val varFileArgs = mutableListOf<String>()

        // 既存のvarFileがある場合（planFileがない場合のみ）
        if (planFile == null && config.hasVarFile()) {
            varFileArgs.add("-var-file=${config.varFile!!.absolutePath}")
        }

        // 生成された.tfvarsファイルがある場合
        if (generatedTfvarsFile != null) {
            varFileArgs.add("-var-file=${generatedTfvarsFile.absolutePath}")
        }

        val planArgs = if (planFile != null) arrayOf(planFile) else emptyArray()

        val args = baseArgs + additionalArgs + varFileArgs + planArgs

        return processExecutor.execute(
            args = args,
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
     * .tfvarsファイルを保存
     */
    private fun saveTfvarsFile(config: TerraformConfig, content: String): File {
        val tfvarsFile = File(config.workingDirectory, "secrets.tfvars")
        tfvarsFile.writeText(content)
        return tfvarsFile
    }
}