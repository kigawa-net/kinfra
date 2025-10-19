package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.action.bitwarden.BitwardenRepository
import net.kigawa.kinfra.action.execution.ActionExecutor
import net.kigawa.kinfra.action.execution.DeploymentPipeline
import net.kigawa.kinfra.action.execution.ExecutionStep
import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.conf.R2BackendConfig
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.isSuccess
import java.io.File

class DeployAction(
    private val terraformService: TerraformService,
    private val bitwardenRepository: BitwardenRepository,
    private val logger: Logger
) : Action {
    
    private val executor = ActionExecutor(logger)
    private val pipeline = DeploymentPipeline(terraformService, bitwardenRepository)
    
    override fun execute(args: List<String>): Int {
        val additionalArgs = args.filter { it != "--auto-selected" }

        println("${AnsiColors.BLUE}Starting full deployment pipeline${AnsiColors.RESET}")
        println()

        val steps = listOf(
            ExecutionStep("Setup backend") { pipeline.setupBackendIfNeeded() },
            ExecutionStep("Initialize Terraform") { pipeline.initializeTerraform(additionalArgs) },
            ExecutionStep("Create execution plan") { pipeline.createExecutionPlan(additionalArgs) },
            ExecutionStep("Apply changes") { pipeline.applyChanges(additionalArgs) }
        )

        val result = executor.executeSteps(steps)
        
        // Handle post-deployment actions
        if (result == 0) {
            handleSuccessfulDeployment()
        }
        
        return result
    }
    
    private fun handleSuccessfulDeployment() {
        println()
        println("${AnsiColors.GREEN}✅ Deployment completed successfully!${AnsiColors.RESET}")

        // Auto git push after successful deployment
        println()
        println("${AnsiColors.BLUE}Pushing to remote repository...${AnsiColors.RESET}")
        val pushResult = pipeline.pushToGit()
        if (pushResult != 0) {
            println("${AnsiColors.YELLOW}⚠${AnsiColors.RESET} Failed to push to remote repository (non-fatal)")
        }
    }

override fun getDescription(): String {
        return "Full deployment pipeline (init → plan → apply)"
    }

    private fun setupR2BackendIfNeeded(): Boolean {
        val backendFile = File("backend.tfvars")

        // Check if backend.tfvars already exists and is valid
        if (backendFile.exists()) {
            val content = backendFile.readText()
            if (!content.contains("<account-id>") && !content.contains("your-r2-")) {
                println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Backend configuration already exists")
                return true
            }
        }

        println("${AnsiColors.YELLOW}Backend configuration not found or contains placeholders${AnsiColors.RESET}")
        println("${AnsiColors.BLUE}Fetching credentials from Bitwarden...${AnsiColors.RESET}")

        // Check if bw is installed
        if (!bitwardenRepository.isInstalled()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Bitwarden CLI (bw) is not installed.")
            println("${AnsiColors.BLUE}Install with:${AnsiColors.RESET} npm install -g @bitwarden/cli")
            return false
        }

        // Check if logged in
        if (!bitwardenRepository.isLoggedIn()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Not logged in to Bitwarden.")
            println("${AnsiColors.BLUE}Please run:${AnsiColors.RESET} bw login")
            return false
        }

        // Get session token - prioritize file over environment variable
        val session = bitwardenRepository.getSessionFromFile()
            ?: bitwardenRepository.getSessionFromEnv()

        if (session == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} No Bitwarden session found.")
            println()
            println("${AnsiColors.BLUE}Please unlock Bitwarden:${AnsiColors.RESET}")
            println("  ./gradlew run --args=\"login\"")
            println()
            println("${AnsiColors.BLUE}Or set BW_SESSION manually:${AnsiColors.RESET}")
            println("  export BW_SESSION=\$(bw unlock --raw)")
            println()
            println("${AnsiColors.BLUE}Then run the deploy command again:${AnsiColors.RESET}")
            println("  ./gradlew run --args=\"deploy\"")
            return false
        }

        val sessionSource = if (bitwardenRepository.getSessionFromFile() != null) "session file" else "environment"
        println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Using BW_SESSION from $sessionSource")

        // Get the item (using default name)
        val itemName = "Cloudflare R2 Terraform Backend"
        val item = bitwardenRepository.getItem(itemName, session)

        if (item == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Item '$itemName' not found in Bitwarden.")
            println()
            println("${AnsiColors.YELLOW}Options:${AnsiColors.RESET}")
            println("1. Create the item manually in Bitwarden with the following fields:")
            println("   - Name: $itemName")
            println("   - Fields: access_key, secret_key, account_id, bucket_name")
            println()

            println("3. Or use the SDK-based deploy command (recommended if using BW_PROJECT):")
            println("   ${AnsiColors.BLUE}export BWS_ACCESS_TOKEN=<your-token>${AnsiColors.RESET}")
            println("   ${AnsiColors.BLUE}./gradlew run --args=\"deploy-sdk\"${AnsiColors.RESET}")
            return false
        }

        // Extract credentials
        val accessKey = item.getFieldValue("access_key")
        val secretKey = item.getFieldValue("secret_key")
        val accountId = item.getFieldValue("account_id")
        val bucketName = item.getFieldValue("bucket_name") ?: "kigawa-infra-state"

        // Validate credentials
        if (accessKey == null || secretKey == null || accountId == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Missing required fields in Bitwarden item.")
            return false
        }

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

        println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Backend configuration created successfully")
        println()

        return true
    }

    private fun gitPush(): Boolean {
        return try {
            val process = ProcessBuilder("git", "push")
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            val exitCode = process.waitFor()
            if (exitCode != 0) {
                val error = process.errorStream.bufferedReader().readText()
                println("${AnsiColors.YELLOW}Git push failed: $error${AnsiColors.RESET}")
                false
            } else {
                true
            }
        } catch (e: Exception) {
            println("${AnsiColors.YELLOW}Git push error: ${e.message}${AnsiColors.RESET}")
            false
        }
    }
}
