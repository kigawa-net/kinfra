package net.kigawa.kinfra.commands

import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.infrastructure.bitwarden.BitwardenRepository
import net.kigawa.kinfra.model.Command
import net.kigawa.kinfra.model.R2BackendConfig
import net.kigawa.kinfra.util.AnsiColors
import java.io.File

class DeployCommand(
    private val terraformService: TerraformService,
    private val bitwardenRepository: BitwardenRepository
) : Command {
    override fun execute(args: Array<String>): Int {
        val additionalArgs = args.filter { it != "--auto-selected" }.toTypedArray()

        println("${AnsiColors.BLUE}Starting full deployment pipeline${AnsiColors.RESET}")
        println()

        // Step 0: Setup R2 backend if needed
        if (!setupR2BackendIfNeeded()) {
            return 1
        }

        // Step 1: Initialize
        println("${AnsiColors.BLUE}Step 1/3: Initializing Terraform${AnsiColors.RESET}")
        val initResult = terraformService.init(quiet = false)
        if (initResult.isFailure) return initResult.exitCode

        println()

        // Step 2: Plan
        println("${AnsiColors.BLUE}Step 2/3: Creating execution plan${AnsiColors.RESET}")
        val planResult = terraformService.plan(additionalArgs, quiet = false)
        if (planResult.isFailure) return planResult.exitCode

        println()

        // Step 3: Apply
        println("${AnsiColors.BLUE}Step 3/3: Applying changes${AnsiColors.RESET}")
        val applyArgsWithAutoApprove = if (additionalArgs.contains("-auto-approve")) {
            additionalArgs
        } else {
            additionalArgs + "-auto-approve"
        }
        val applyResult = terraformService.apply(additionalArgs = applyArgsWithAutoApprove, quiet = false)

        if (applyResult.isSuccess) {
            println()
            println("${AnsiColors.GREEN}✅ Deployment completed successfully!${AnsiColors.RESET}")

            // Auto git push after successful deployment
            println()
            println("${AnsiColors.BLUE}Pushing to remote repository...${AnsiColors.RESET}")
            val pushResult = gitPush()
            if (pushResult) {
                println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Successfully pushed to remote repository")
            } else {
                println("${AnsiColors.YELLOW}⚠${AnsiColors.RESET} Failed to push to remote repository (non-fatal)")
            }
        }

        return applyResult.exitCode
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
            println("2. Run the setup command:")
            println("   ${AnsiColors.BLUE}./gradlew run --args=\"setup-r2\"${AnsiColors.RESET}")
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