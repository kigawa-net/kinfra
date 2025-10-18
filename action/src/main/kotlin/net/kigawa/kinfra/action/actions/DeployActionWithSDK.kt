package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.action.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.action.config.EnvFileLoader
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.conf.R2BackendConfig
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.exitCode
import net.kigawa.kinfra.model.util.isFailure
import net.kigawa.kinfra.model.util.isSuccess
import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.model.BitwardenSecret
import java.io.File

/**
 * Bitwarden Secret Manager SDK を使用したデプロイコマンド
 */
class DeployActionWithSDK(
    private val terraformService: TerraformService,
    private val secretManagerRepository: BitwardenSecretManagerRepository,
    private val logger: Logger,
    private val envFileLoader: EnvFileLoader
): Action {
    override fun execute(args: Array<String>): Int {
        logger.info("DeployActionWithSDK started with args: ${args.joinToString(" ")}")

        val additionalArgs = args.filter { it != "--auto-selected" }.toTypedArray()

        println("${AnsiColors.BLUE}Starting full deployment pipeline${AnsiColors.RESET}")
        println()

        // Step 0: Setup R2 backend if needed
        logger.info("Step 0: Checking R2 backend configuration")
        if (!setupR2BackendIfNeeded()) {
            logger.error("Failed to setup R2 backend")
            return 1
        }

        // Step 1: Initialize
        logger.info("Step 1: Initializing Terraform")
        println("${AnsiColors.BLUE}Step 1/3: Initializing Terraform${AnsiColors.RESET}")
        val initResult = terraformService.init()
        if (initResult.isFailure()) {
            logger.error("Terraform init failed with exit code: ${initResult.exitCode()}")
            return initResult.exitCode()
        }
        logger.info("Terraform init completed successfully")

        println()

        // Step 2: Plan
        logger.info("Step 2: Creating execution plan")
        println("${AnsiColors.BLUE}Step 2/3: Creating execution plan${AnsiColors.RESET}")
        val planResult = terraformService.plan(additionalArgs)
        if (planResult.isFailure()) {
            logger.error("Terraform plan failed with exit code: ${planResult.exitCode()}")
            return planResult.exitCode()
        }
        logger.info("Terraform plan completed successfully")

        println()

        // Step 3: Apply
        logger.info("Step 3: Applying changes")
        println("${AnsiColors.BLUE}Step 3/3: Applying changes${AnsiColors.RESET}")
        val applyArgsWithAutoApprove = if (additionalArgs.contains("-auto-approve")) {
            additionalArgs
        } else {
            additionalArgs + "-auto-approve"
        }
        val applyResult = terraformService.apply(additionalArgs = applyArgsWithAutoApprove)

        if (applyResult.isSuccess()) {
            logger.info("Deployment completed successfully")
            println()
            println("${AnsiColors.GREEN}✅ Deployment completed successfully!${AnsiColors.RESET}")

            // Auto git push after successful deployment
            println()
            println("${AnsiColors.BLUE}Pushing to remote repository...${AnsiColors.RESET}")
            val pushResult = gitPush()
            if (pushResult) {
                logger.info("Successfully pushed to remote repository")
                println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Successfully pushed to remote repository")
            } else {
                logger.warn("Failed to push to remote repository")
                println("${AnsiColors.YELLOW}⚠${AnsiColors.RESET} Failed to push to remote repository (non-fatal)")
            }
        } else {
            logger.error("Terraform apply failed with exit code: ${applyResult.exitCode()}")
        }

        return applyResult.exitCode()
    }

    override fun getDescription(): String {
        return "Full deployment pipeline using Secret Manager SDK (init → plan → apply)"
    }

    private fun setupR2BackendIfNeeded(): Boolean {
        logger.debug("Checking R2 backend configuration")
        val backendFile = File("backend.tfvars")

        // Check if backend.tfvars already exists and is valid
        if (backendFile.exists()) {
            val content = backendFile.readText()
            if (!content.contains("<account-id>") && !content.contains("your-r2-")) {
                logger.info("Backend configuration already exists and is valid")
                println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Backend configuration already exists")
                return true
            }
        }

        logger.warn("Backend configuration not found or contains placeholders")
        println("${AnsiColors.YELLOW}Backend configuration not found or contains placeholders${AnsiColors.RESET}")
        println("${AnsiColors.BLUE}Fetching credentials from Bitwarden Secret Manager...${AnsiColors.RESET}")

        // プロジェクトIDを.envまたは環境変数から取得
        val projectId = envFileLoader.get("BW_PROJECT")
        if (projectId != null) {
            logger.info("Using project ID from .env: $projectId")
            println("${AnsiColors.BLUE}Using project ID from .env: ${projectId}${AnsiColors.RESET}")
        }

        // シークレットを取得（リトライ付き）
        val maxAttempts = 3
        var lastError: Exception? = null
        val secrets = run {
            var result: List<BitwardenSecret>? = null
            var delayMs = 1000L
            for (attempt in 1..maxAttempts) {
                try {
                    logger.debug("Fetching secrets from Bitwarden Secret Manager (attempt $attempt/$maxAttempts)")
                    result = secretManagerRepository.listSecrets()
                    break
                } catch (e: Exception) {
                    lastError = e
                    val message = e.message ?: e::class.simpleName ?: "Unknown error"
                    println("${AnsiColors.YELLOW}Warn:${AnsiColors.RESET} Failed to fetch secrets (attempt $attempt/$maxAttempts): $message")
                    if (attempt < maxAttempts) {
                        try {
                            Thread.sleep(delayMs)
                        } catch (_: InterruptedException) { /* ignore */ }
                        delayMs *= 2
                    }
                }
            }
            result
        }
        if (secrets == null) {
            val e = lastError
            if (e != null) {
                logger.error("Failed to fetch secrets from Bitwarden after $maxAttempts attempts", e)
            } else {
                logger.error("Failed to fetch secrets from Bitwarden after $maxAttempts attempts")
            }
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to fetch secrets after $maxAttempts attempts: ${lastError?.message}")
            println()
            println("${AnsiColors.BLUE}Make sure BWS_ACCESS_TOKEN environment variable is set${AnsiColors.RESET}")
            if (projectId != null) {
                println("${AnsiColors.BLUE}Using BW_PROJECT from .env: ${projectId}${AnsiColors.RESET}")
            }
            return false
        }

        logger.debug("Retrieved ${secrets.size} secrets from Bitwarden")

        // R2認証情報を検索
        val accessKeySecret = secrets.find { it.key == "r2-access" }
        val secretKeySecret = secrets.find { it.key == "r2-secret" }
        val accountSecret = secrets.find { it.key == "r2-account" }
        val bucketSecret = secrets.find { it.key == "r2-bucket" }

        if (accessKeySecret == null || secretKeySecret == null || accountSecret == null) {
            logger.error("Required R2 secrets not found in Secret Manager")
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Required secrets not found in Secret Manager.")
            println()
            println("${AnsiColors.YELLOW}Required secret keys and formats:${AnsiColors.RESET}")
            println("  - r2-access: R2 Access Key ID (32-char hex, e.g. f187f7a87ac5ab2d425bfd783e11146f)")
            println("  - r2-secret: R2 Secret Access Key (64-char hex)")
            println("  - r2-account: R2 Account ID (32-char hex, e.g. e9f30fd43ef4cc3d46050e34dad5c811)")
            println("  - r2-bucket: Bucket name (optional, e.g. kigawa-infra-state, NOT a URL)")
            println()
            println("${AnsiColors.BLUE}Available secrets:${AnsiColors.RESET}")
            secrets.forEach { println("  - ${it.key}") }
            return false
        }

        val accessKey = accessKeySecret.value
        val secretKey = secretKeySecret.value
        val accountId = accountSecret.value
        val bucketName = bucketSecret?.value ?: "kigawa-infra-state"

        logger.info("Successfully retrieved R2 credentials from Secret Manager")
        logger.debug("R2 config - Bucket: $bucketName, Account: ${accountId.take(10)}...")

        println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Credentials retrieved from Secret Manager")
        println("${AnsiColors.BLUE}Debug - Secret values:${AnsiColors.RESET}")
        println("  r2-access (Access Key ID): ${accessKey.take(10)}...")
        println("  r2-secret (Secret Key): ${secretKey.take(10)}...")
        println("  r2-account (Account ID): ${accountId.take(10)}...")
        println("  r2-bucket (Bucket): $bucketName")

        // Create backend config
        val config = R2BackendConfig(
            bucket = bucketName,
            key = "terraform.tfstate",
            endpoint = "https://$accountId.r2.cloudflarestorage.com",
            accessKey = accessKey,
            secretKey = secretKey
        )

        // Save to file
        backendFile.parentFile?.mkdirs()
        backendFile.writeText(config.toTfvarsContent())
        backendFile.setReadable(true, true)
        backendFile.setWritable(true, true)

        logger.info("Backend configuration file created: ${backendFile.absolutePath}")
        println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Backend configuration created successfully")
        println()

        return true
    }

    private fun gitPush(): Boolean {
        return try {
            logger.debug("Executing git push")
            val process = ProcessBuilder("git", "push")
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            val exitCode = process.waitFor()
            if (exitCode != 0) {
                val error = process.errorStream.bufferedReader().readText()
                logger.warn("Git push failed with exit code $exitCode: $error")
                println("${AnsiColors.YELLOW}Git push failed: $error${AnsiColors.RESET}")
                false
            } else {
                logger.debug("Git push completed successfully")
                true
            }
        } catch (e: Exception) {
            logger.error("Git push error", e)
            println("${AnsiColors.YELLOW}Git push error: ${e.message}${AnsiColors.RESET}")
            false
        }
    }
}
