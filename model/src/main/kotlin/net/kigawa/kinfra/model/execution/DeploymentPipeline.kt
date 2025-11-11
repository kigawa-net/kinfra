package net.kigawa.kinfra.model.execution

import net.kigawa.kinfra.model.BitwardenItem
import net.kigawa.kinfra.model.bitwarden.BitwardenRepository
import net.kigawa.kinfra.model.conf.R2BackendConfig
import net.kigawa.kinfra.model.logging.Logger
import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.exitCode
import net.kigawa.kinfra.model.util.isFailure
import net.kigawa.kinfra.model.util.message
import net.kigawa.kinfra.model.GitHelper
import net.kigawa.kodel.api.err.Res
import net.kigawa.kodel.api.err.ActionException
import java.io.File

private const val TERRAFORM_CONFIG_NOT_FOUND = "Terraform configuration not found"
private const val AUTO_APPROVE_FLAG = "-auto-approve"

/**
 * デプロイパイプラインの各ステップを担当するクラス
 */
class DeploymentPipeline(
    private val terraformService: TerraformService,
    private val bitwardenRepository: BitwardenRepository,
    private val logger: Logger,
    private val gitHelper: GitHelper,
) {
    private fun handleTerraformResult(result: Res<Int, ActionException>, operation: String): Int {
        return if (result.isFailure()) {
            if (result.message()?.contains(TERRAFORM_CONFIG_NOT_FOUND) == true) {
                0
            } else {
                logger.error("$operation failed: ${result.message()} (exit code: ${result.exitCode()})")
                result.exitCode()
            }
        } else {
            0
        }
    }

    fun initializeTerraform(additionalArgs: List<String>): Int {
        logger.debug("Calling terraformService.init")
        val result = terraformService.init(additionalArgs = additionalArgs)
        logger.debug("terraformService.init returned: $result")
        return handleTerraformResult(result, "Terraform init")
    }

    fun createExecutionPlan(additionalArgs: List<String>): Int {
        logger.debug("Calling terraformService.plan")
        val result = terraformService.plan(additionalArgs)
        logger.debug("terraformService.plan returned: $result")
        return handleTerraformResult(result, "Terraform plan")
    }

    fun applyChanges(additionalArgs: List<String>): Int {
        val applyArgsWithAutoApprove =
            if (additionalArgs.contains(AUTO_APPROVE_FLAG)) {
                additionalArgs
            } else {
                additionalArgs + AUTO_APPROVE_FLAG
            }
        logger.debug("Calling terraformService.apply with args: $applyArgsWithAutoApprove")
        val result = terraformService.apply(additionalArgs = applyArgsWithAutoApprove)
        logger.debug("terraformService.apply returned: $result")
        return handleTerraformResult(result, "Terraform apply")
    }

    fun pushToGit(): Int {
        return if (gitHelper.pushToRemote()) {
            logger.info("Successfully pushed to remote repository")
            0
        } else {
            logger.warn("Git push failed")
            1
        }
    }

    /**
     * バックエンドセットアップを担当するクラス
     */
    private inner class BackendSetup {
        fun setup(): Int {
        logger.warn("Backend configuration not found or contains placeholders")
        logger.info("Fetching credentials from Bitwarden...")

        // Check if bw is installed
        if (!bitwardenRepository.isInstalled()) {
            logger.error("Bitwarden CLI (bw) is not installed.")
            logger.info("Install with: npm install -g @bitwarden/cli")
            return 1
        }

        // Check if logged in
        if (!bitwardenRepository.isLoggedIn()) {
            logger.error("Not logged in to Bitwarden.")
            logger.info("Please run: bw login")
            return 1
        }

        val session = getSession()
        if (session == null) {
            showSessionError()
            return 1
        }

        val item = getItem(session)
        if (item == null) {
            showItemNotFoundError()
            return 1
        }

        return createBackendFile(item)
        }

        private fun getSession(): String? {
        return bitwardenRepository.getSessionFromFile()
            ?: bitwardenRepository.getSessionFromEnv()
    }

        private fun showSessionError() {
            logger.error("No Bitwarden session found.")
            logger.info("Please unlock Bitwarden:")
            logger.info("  ./gradlew run --args=\"login\"")
            logger.info("Or set BW_SESSION manually:")
            logger.info("  export BW_SESSION=\$(bw unlock --raw)")
            logger.info("Then run deploy command again:")
            logger.info("  ./gradlew run --args=\"deploy\"")
        }

        private fun getItem(session: String) = bitwardenRepository.getItem("Cloudflare R2 Terraform Backend", session)

            private fun showItemNotFoundError() {
            logger.error("Item 'Cloudflare R2 Terraform Backend' not found in Bitwarden.")
            logger.warn("Options:")
            logger.info("1. Create item manually in Bitwarden with following fields:")
            logger.info("   - Name: Cloudflare R2 Terraform Backend")
            logger.info("   - Fields: access_key, secret_key, account_id, bucket_name")
            logger.info("3. Or use SDK-based deploy command (recommended if using BW_PROJECT):")
            logger.info("  export BWS_ACCESS_TOKEN=<your-token>")
            logger.info("  ./gradlew run --args=\"deploy-sdk\"")
        }

            private fun createBackendFile(item: BitwardenItem): Int {
            val accessKey = item.getFieldValue("access_key")
            val secretKey = item.getFieldValue("secret_key")
            val accountId = item.getFieldValue("account_id")
            val bucketName = item.getFieldValue("bucket_name") ?: "kigawa-infra-state"

            // Validate credentials
            if (accessKey == null || secretKey == null || accountId == null) {
                logger.error("Missing required fields in Bitwarden item.")
                return 1
            }

            // Create backend config
            val config =
                R2BackendConfig(
                    bucket = bucketName,
                    key = "terraform.tfstate",
                    endpoint = "https://$accountId.r2.cloudflarestorage.com",
                    accessKey = accessKey,
                    secretKey = secretKey,
                )

            // Save to file
            val backendFile = File("backend.tfvars")
            backendFile.parentFile?.mkdirs()
            backendFile.writeText(config.toTfvarsContent())
            backendFile.setReadable(true, true)
            backendFile.setWritable(true, true)

            logger.info("Backend configuration created successfully")
            return 0
        }
    }
}
