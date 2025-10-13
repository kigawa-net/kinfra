package net.kigawa.kinfra.commands

import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.model.Command
import net.kigawa.kinfra.model.conf.R2BackendConfig
import net.kigawa.kinfra.infrastructure.bitwarden.BitwardenRepository
import net.kigawa.kinfra.util.AnsiColors
import java.io.File

class SetupR2Command(
    private val bitwardenRepository: BitwardenRepository,
    private val gitHelper: GitHelper
) : Command {

    override fun execute(args: Array<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

        println("${AnsiColors.BLUE}=== Cloudflare R2 Backend Setup ===${AnsiColors.RESET}")
        println()

        // Check if bw is installed
        if (!bitwardenRepository.isInstalled()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Bitwarden CLI (bw) is not installed.")
            println("${AnsiColors.BLUE}Install with:${AnsiColors.RESET}")
            println("  npm install -g @bitwarden/cli")
            println("  # or")
            println("  snap install bw")
            return 1
        }

        // Check if logged in
        if (!bitwardenRepository.isLoggedIn()) {
            println("${AnsiColors.YELLOW}Not logged in to Bitwarden.${AnsiColors.RESET}")
            println("${AnsiColors.BLUE}Please run:${AnsiColors.RESET} bw login")
            return 1
        }

        // Get session token
        println("${AnsiColors.BLUE}Unlocking Bitwarden vault...${AnsiColors.RESET}")
        print("Enter your Bitwarden master password: ")
        val password = System.console()?.readPassword()?.let { String(it) } ?: run {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to read password")
            return 1
        }

        val session = bitwardenRepository.unlock(password)
        if (session == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to unlock Bitwarden vault.")
            return 1
        }

        println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Vault unlocked successfully")
        println()

        // Prompt for item name
        print("${AnsiColors.BLUE}Enter the name of the Bitwarden item containing R2 credentials${AnsiColors.RESET}\n(default: 'Cloudflare R2 Terraform Backend'): ")
        val itemName = readLine()?.takeIf { it.isNotBlank() } ?: "Cloudflare R2 Terraform Backend"

        // Get the item
        println("${AnsiColors.BLUE}Fetching credentials from Bitwarden...${AnsiColors.RESET}")
        val item = bitwardenRepository.getItem(itemName, session)

        if (item == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Item '$itemName' not found in Bitwarden.")
            println()
            println("${AnsiColors.YELLOW}Available items (first 10):${AnsiColors.RESET}")
            val items = bitwardenRepository.listItems(session)
            items.take(10).forEach { println("  - ${it.name}") }
            println()
            println("${AnsiColors.BLUE}Note:${AnsiColors.RESET} If you're using Bitwarden projects (BW_PROJECT in .env),")
            println("consider using the SDK-based command instead:")
            println("  ${AnsiColors.BLUE}export BWS_ACCESS_TOKEN=<your-token>${AnsiColors.RESET}")
            println("  ${AnsiColors.BLUE}./gradlew run --args=\"setup-r2-sdk\"${AnsiColors.RESET}")
            return 1
        }

        // Extract credentials
        val accessKey = item.getFieldValue("access_key")
        val secretKey = item.getFieldValue("secret_key")
        val accountId = item.getFieldValue("account_id")
        val bucketName = item.getFieldValue("bucket_name") ?: "kigawa-infra-state"

        // Validate credentials
        if (accessKey == null || secretKey == null || accountId == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Missing required fields in Bitwarden item.")
            println()
            println("${AnsiColors.YELLOW}Required fields:${AnsiColors.RESET}")
            println("  - access_key")
            println("  - secret_key")
            println("  - account_id")
            println("  - bucket_name (optional, defaults to 'kigawa-infra-state')")
            println()
            println("${AnsiColors.BLUE}Current fields:${AnsiColors.RESET}")
            item.fields.forEach { field ->
                val displayValue = if (field.type == 1) "***" else field.value
                println("  - ${field.name}: $displayValue")
            }
            return 1
        }

        println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Credentials retrieved successfully")
        println()

        // Prompt for environment
        println("${AnsiColors.BLUE}Select environment:${AnsiColors.RESET}")
        println("  1) prod (recommended)")
        println("  2) global (root backend.tfvars)")
        print("Choice [1]: ")
        val envChoice = readLine()?.takeIf { it.isNotBlank() } ?: "1"

        val (backendFile, stateKey) = when (envChoice) {
            "1" -> Pair("environments/prod/backend.tfvars", "prod/terraform.tfstate")
            "2" -> Pair("backend.tfvars", "terraform.tfstate")
            else -> {
                println("${AnsiColors.RED}Invalid choice${AnsiColors.RESET}")
                return 1
            }
        }

        // Create backend config
        val config = R2BackendConfig(
            bucket = bucketName,
            key = stateKey,
            endpoint = "https://$accountId.r2.cloudflarestorage.com",
            accessKey = accessKey,
            secretKey = secretKey
        )

        // Save to file
        println("${AnsiColors.BLUE}Creating backend configuration file...${AnsiColors.RESET}")
        val backendConfigFile = File(backendFile)
        backendConfigFile.parentFile?.mkdirs()
        backendConfigFile.writeText(config.toTfvarsContent())

        // Set file permissions (owner read/write only)
        backendConfigFile.setReadable(true, true)
        backendConfigFile.setWritable(true, true)

        println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Backend configuration saved to: $backendFile")
        println()

        // Show next steps
        println("${AnsiColors.BLUE}=== Next Steps ===${AnsiColors.RESET}")
        println()
        println("1. Initialize Terraform with R2 backend:")
        println("   ${AnsiColors.GREEN}./terraform.sh init prod${AnsiColors.RESET}")
        println()
        println("2. If migrating from local state, Terraform will ask:")
        println("   ${AnsiColors.YELLOW}\"Do you want to copy existing state to the new backend?\"${AnsiColors.RESET}")
        println("   Answer: ${AnsiColors.GREEN}yes${AnsiColors.RESET}")
        println()
        println("3. Verify the setup:")
        println("   ${AnsiColors.GREEN}./terraform.sh plan prod${AnsiColors.RESET}")
        println()

        println("${AnsiColors.GREEN}Setup complete!${AnsiColors.RESET}")

        return 0
    }

    override fun getDescription(): String {
        return "Setup Cloudflare R2 backend configuration using Bitwarden CLI"
    }
}
