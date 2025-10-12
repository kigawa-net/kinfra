package net.kigawa.kinfra.infrastructure.service

import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.model.CommandResult
import net.kigawa.kinfra.model.TerraformConfig
import net.kigawa.kinfra.model.FilePaths
import net.kigawa.kinfra.infrastructure.process.ProcessExecutor
import net.kigawa.kinfra.infrastructure.terraform.TerraformRepository

/**
 * TerraformServiceの実装
 */
class TerraformServiceImpl(
    private val processExecutor: ProcessExecutor,
    private val terraformRepository: TerraformRepository
) : TerraformService {

    override fun init(additionalArgs: Array<String>, quiet: Boolean): CommandResult {
        val config = terraformRepository.getTerraformConfig()

        // Check for backend config file
        val backendConfigArgs = findBackendConfigFile()?.let {
            arrayOf("-backend-config=$it")
        } ?: emptyArray()

        val args = arrayOf("terraform", "init") + backendConfigArgs + additionalArgs

        return processExecutor.execute(
            args = args,
            workingDir = config.workingDirectory,
            environment = mapOf("SSH_CONFIG" to config.sshConfigPath),
            quiet = quiet
        )
    }

    private fun findBackendConfigFile(): String? {
        val backendConfig = java.io.File(FilePaths.BACKEND_TFVARS_FILE)
        if (backendConfig.exists()) {
            return backendConfig.absolutePath
        }

        return null
    }

    override fun plan(additionalArgs: Array<String>, quiet: Boolean): CommandResult {
        val config = terraformRepository.getTerraformConfig()
        val varFileArgs = if (config.hasVarFile()) {
            arrayOf("-var-file=${config.varFile!!.absolutePath}")
        } else {
            emptyArray()
        }

        val args = arrayOf("terraform", "plan") + varFileArgs + additionalArgs

        return processExecutor.execute(
            args = args,
            workingDir = config.workingDirectory,
            environment = mapOf("SSH_CONFIG" to config.sshConfigPath),
            quiet = quiet
        )
    }

    override fun apply(planFile: String?, additionalArgs: Array<String>, quiet: Boolean): CommandResult {
        val config = terraformRepository.getTerraformConfig()

        val baseArgs = arrayOf("terraform", "apply")
        val varFileArgs = if (planFile == null && config.hasVarFile()) {
            arrayOf("-var-file=${config.varFile!!.absolutePath}")
        } else {
            emptyArray()
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

    override fun destroy(additionalArgs: Array<String>, quiet: Boolean): CommandResult {
        val config = terraformRepository.getTerraformConfig()
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

    override fun format(recursive: Boolean, quiet: Boolean): CommandResult {
        val args = if (recursive) {
            arrayOf("terraform", "fmt", "-recursive")
        } else {
            arrayOf("terraform", "fmt")
        }

        return processExecutor.execute(args = args, quiet = quiet)
    }

    override fun validate(quiet: Boolean): CommandResult {
        return processExecutor.execute(args = arrayOf("terraform", "validate"), quiet = quiet)
    }

    override fun show(additionalArgs: Array<String>, quiet: Boolean): CommandResult {
        val config = terraformRepository.getTerraformConfig()
        val args = arrayOf("terraform", "show") + additionalArgs

        return processExecutor.execute(
            args = args,
            workingDir = config.workingDirectory,
            environment = mapOf("SSH_CONFIG" to config.sshConfigPath),
            quiet = quiet
        )
    }

    override fun getTerraformConfig(): TerraformConfig {
        return terraformRepository.getTerraformConfig()
    }
}