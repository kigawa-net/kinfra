package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.conf.R2BackendConfig
import net.kigawa.kinfra.action.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.action.config.EnvFileLoader
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File

/**
 * Bitwarden Secret Manager SDK を使用したR2バックエンドセットアップコマンド
 */
class SetupR2ActionWithSDK(
    private val secretManagerRepository: BitwardenSecretManagerRepository,
    private val gitHelper: GitHelper,
    private val envFileLoader: EnvFileLoader
) : Action {

    override fun execute(args: Array<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

        println("${AnsiColors.BLUE}=== Cloudflare R2 Backend Setup (Secret Manager SDK) ===${AnsiColors.RESET}")
        println()

        // プロジェクトIDを引数、環境変数、.envファイルから取得
        val projectId = if (args.isNotEmpty()) {
            args[0]
        } else {
            envFileLoader.get("BW_PROJECT") ?: System.getenv("BW_PROJECT_ID")
        }

        if (projectId != null) {
            println("${AnsiColors.BLUE}Using project ID: ${projectId}${AnsiColors.RESET}")
        }

        println("${AnsiColors.BLUE}Fetching secrets from Bitwarden Secret Manager...${AnsiColors.RESET}")

        val secrets = try {
            secretManagerRepository.listSecrets()
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to fetch secrets: ${e.message}")
            return 1
        }

        if (secrets.isEmpty()) {
            println("${AnsiColors.YELLOW}No secrets found in Secret Manager${AnsiColors.RESET}")
            return 1
        }

        println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Found ${secrets.size} secrets")
        println()

        // R2認証情報を検索
        val accessKeySecret = secrets.find { it.key == "r2-access" }
        val secretKeySecret = secrets.find { it.key == "r2-secret" }
        val accountSecret = secrets.find { it.key == "r2-account" }
        val bucketSecret = secrets.find { it.key == "r2-bucket" }

        // 利用可能なシークレットキーを表示
        if (accessKeySecret == null || secretKeySecret == null || accountSecret == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Required secrets not found.")
            println()
            println("${AnsiColors.YELLOW}Required secret keys and formats:${AnsiColors.RESET}")
            println("  - r2-access: R2 Access Key ID (32-char hex)")
            println("  - r2-secret: R2 Secret Access Key (64-char hex)")
            println("  - r2-account: R2 Account ID (32-char hex)")
            println("  - r2-bucket: Bucket name (optional, defaults to 'kigawa-infra-state', NOT a URL)")
            println()
            println("${AnsiColors.BLUE}Available secrets:${AnsiColors.RESET}")
            secrets.forEach { println("  - ${it.key}") }
            return 1
        }

        val accessKey = accessKeySecret.value
        val secretKey = secretKeySecret.value
        val accountId = accountSecret.value
        val bucketName = bucketSecret?.value ?: "kigawa-infra-state"

        println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Credentials retrieved successfully")
        println()

        // 環境選択
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

        // バックエンド設定を作成
        val config = R2BackendConfig(
            bucket = bucketName,
            key = stateKey,
            endpoint = "https://$accountId.r2.cloudflarestorage.com",
            accessKey = accessKey,
            secretKey = secretKey
        )

        // ファイルに保存
        println("${AnsiColors.BLUE}Creating backend configuration file...${AnsiColors.RESET}")
        val backendConfigFile = File(backendFile)
        backendConfigFile.parentFile?.mkdirs()
        backendConfigFile.writeText(config.toTfvarsContent())

        // ファイル権限を設定 (owner read/write only)
        backendConfigFile.setReadable(true, true)
        backendConfigFile.setWritable(true, true)

        println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Backend configuration saved to: $backendFile")
        println()

        // 次のステップを表示
        println("${AnsiColors.BLUE}=== Next Steps ===${AnsiColors.RESET}")
        println()
        println("1. Initialize Terraform with R2 backend:")
        println("   ${AnsiColors.GREEN}./gradlew run --args=\"init prod\"${AnsiColors.RESET}")
        println()
        println("2. If migrating from local state, Terraform will ask:")
        println("   ${AnsiColors.YELLOW}\"Do you want to copy existing state to the new backend?\"${AnsiColors.RESET}")
        println("   Answer: ${AnsiColors.GREEN}yes${AnsiColors.RESET}")
        println()
        println("3. Verify the setup:")
        println("   ${AnsiColors.GREEN}./gradlew run --args=\"plan prod\"${AnsiColors.RESET}")
        println()

        println("${AnsiColors.GREEN}Setup complete!${AnsiColors.RESET}")

        return 0
    }


    override fun getDescription(): String {
        return "Setup Cloudflare R2 backend configuration using Bitwarden Secret Manager SDK"
    }
}
