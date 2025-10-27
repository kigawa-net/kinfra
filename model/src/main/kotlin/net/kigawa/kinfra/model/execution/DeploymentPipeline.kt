package net.kigawa.kinfra.model.execution

import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.model.bitwarden.BitwardenRepository
import net.kigawa.kinfra.model.BitwardenItem
import net.kigawa.kinfra.model.conf.R2BackendConfig
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.exitCode
import net.kigawa.kinfra.model.util.isFailure
import net.kigawa.kinfra.model.util.message
import java.io.File

/**
 * デプロイパイプラインの各ステップを担当するクラス
 */
class DeploymentPipeline(
    private val terraformService: TerraformService,
    private val bitwardenRepository: BitwardenRepository
) {
    

    
    fun initializeTerraform(additionalArgs: List<String>): Int {
        println("${AnsiColors.BLUE}Calling terraformService.init${AnsiColors.RESET}")
        val result = terraformService.init(additionalArgs = additionalArgs)
        println("${AnsiColors.BLUE}terraformService.init returned: $result${AnsiColors.RESET}")
        return if (result.isFailure()) {
            // Terraform設定がない場合はスキップとして成功扱い
            if (result.message()?.contains("Terraform configuration not found") == true) {
                0
            } else {
                println("${AnsiColors.RED}Terraform init failed: ${result.message()} (exit code: ${result.exitCode()})${AnsiColors.RESET}")
                result.exitCode()
            }
        } else {
            0
        }
    }
    
    fun createExecutionPlan(additionalArgs: List<String>): Int {
        println("${AnsiColors.BLUE}Calling terraformService.plan${AnsiColors.RESET}")
        val result = terraformService.plan(additionalArgs)
        println("${AnsiColors.BLUE}terraformService.plan returned: $result${AnsiColors.RESET}")
        return if (result.isFailure()) {
            // Terraform設定がない場合はスキップとして成功扱い
            if (result.message()?.contains("Terraform configuration not found") == true) {
                0
            } else {
                println("${AnsiColors.RED}Terraform plan failed: ${result.message()} (exit code: ${result.exitCode()})${AnsiColors.RESET}")
                result.exitCode()
            }
        } else {
            0
        }
    }
    
    fun applyChanges(additionalArgs: List<String>): Int {
        val applyArgsWithAutoApprove = if (additionalArgs.contains("-auto-approve")) {
            additionalArgs
        } else {
            additionalArgs + "-auto-approve"
        }
        println("${AnsiColors.BLUE}Calling terraformService.apply with args: $applyArgsWithAutoApprove${AnsiColors.RESET}")
        val result = terraformService.apply(additionalArgs = applyArgsWithAutoApprove)
        println("${AnsiColors.BLUE}terraformService.apply returned: $result${AnsiColors.RESET}")
        return if (result.isFailure()) {
            // Terraform設定がない場合はスキップとして成功扱い
            if (result.message()?.contains("Terraform configuration not found") == true) {
                0
            } else {
                println("${AnsiColors.RED}Terraform apply failed: ${result.message()} (exit code: ${result.exitCode()})${AnsiColors.RESET}")
                result.exitCode()
            }
        } else {
            0
        }
    }
    
    fun pushToGit(): Int {
        return try {
            val process = ProcessBuilder("git", "push")
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            val exitCode = process.waitFor()
            if (exitCode != 0) {
                val error = process.errorStream.bufferedReader().readText()
                println("${AnsiColors.YELLOW}Git push failed: $error${AnsiColors.RESET}")
            } else {
                println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Successfully pushed to remote repository")
            }
            exitCode
        } catch (e: Exception) {
            println("${AnsiColors.YELLOW}Git push error: ${e.message}${AnsiColors.RESET}")
            1
        }
    }
}

/**
 * バックエンドセットアップを担当するクラス
 */
private class BackendSetup(private val bitwardenRepository: BitwardenRepository) {
    
    fun setup(): Int {
        println("${AnsiColors.YELLOW}Backend configuration not found or contains placeholders${AnsiColors.RESET}")
        println("${AnsiColors.BLUE}Fetching credentials from Bitwarden...${AnsiColors.RESET}")

        // Check if bw is installed
        if (!bitwardenRepository.isInstalled()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Bitwarden CLI (bw) is not installed.")
            println("${AnsiColors.BLUE}Install with:${AnsiColors.RESET} npm install -g @bitwarden/cli")
            return 1
        }

        // Check if logged in
        if (!bitwardenRepository.isLoggedIn()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Not logged in to Bitwarden.")
            println("${AnsiColors.BLUE}Please run:${AnsiColors.RESET} bw login")
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
        println("${AnsiColors.RED}Error:${AnsiColors.RESET} No Bitwarden session found.")
        println()
        println("${AnsiColors.BLUE}Please unlock Bitwarden:${AnsiColors.RESET}")
        println("  ./gradlew run --args=\"login\"")
        println()
        println("${AnsiColors.BLUE}Or set BW_SESSION manually:${AnsiColors.RESET}")
        println("  export BW_SESSION=\$(bw unlock --raw)")
        println()
        println("${AnsiColors.BLUE}Then run deploy command again:${AnsiColors.RESET}")
        println("  ./gradlew run --args=\"deploy\"")
    }
    
    private fun getItem(session: String) = bitwardenRepository.getItem("Cloudflare R2 Terraform Backend", session)
    
    private fun showItemNotFoundError() {
        println("${AnsiColors.RED}Error:${AnsiColors.RESET} Item 'Cloudflare R2 Terraform Backend' not found in Bitwarden.")
        println()
        println("${AnsiColors.YELLOW}Options:${AnsiColors.RESET}")
        println("1. Create item manually in Bitwarden with following fields:")
        println("   - Name: Cloudflare R2 Terraform Backend")
        println("   - Fields: access_key, secret_key, account_id, bucket_name")
        println()
        println("3. Or use SDK-based deploy command (recommended if using BW_PROJECT):")
        println("   ${AnsiColors.BLUE}export BWS_ACCESS_TOKEN=<your-token>${AnsiColors.RESET}")
        println("   ${AnsiColors.BLUE}./gradlew run --args=\"deploy-sdk\"${AnsiColors.RESET}")
    }
    
    private fun createBackendFile(item: BitwardenItem): Int {
        val accessKey = item.getFieldValue("access_key")
        val secretKey = item.getFieldValue("secret_key")
        val accountId = item.getFieldValue("account_id")
        val bucketName = item.getFieldValue("bucket_name") ?: "kigawa-infra-state"

        // Validate credentials
        if (accessKey == null || secretKey == null || accountId == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Missing required fields in Bitwarden item.")
            return 1
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
        val backendFile = File("backend.tfvars")
        backendFile.parentFile?.mkdirs()
        backendFile.writeText(config.toTfvarsContent())
        backendFile.setReadable(true, true)
        backendFile.setWritable(true, true)

        println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Backend configuration created successfully")
        println()
        return 0
    }
}