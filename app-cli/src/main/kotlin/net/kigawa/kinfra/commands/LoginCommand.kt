package net.kigawa.kinfra.commands

import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.model.Command
import net.kigawa.kinfra.infrastructure.bitwarden.BitwardenRepository
import net.kigawa.kinfra.infrastructure.config.ConfigRepository
import net.kigawa.kinfra.model.ProjectConfig
import net.kigawa.kinfra.model.KinfraConfig
import net.kigawa.kinfra.model.ProjectInfo
import net.kigawa.kinfra.util.AnsiColors
import java.io.File

class LoginCommand(
    private val bitwardenRepository: BitwardenRepository,
    private val configRepository: ConfigRepository,
    private val gitHelper: GitHelper
) : Command {

    companion object {
        private const val SESSION_FILE = ".bw_session"
        private const val BWS_TOKEN_FILE = ".bws_token"
    }

    override fun execute(args: Array<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

        // GitHubリポジトリ引数が指定されている場合は保存
        if (args.isNotEmpty()) {
            val githubRepo = args[0]
            println("${AnsiColors.BLUE}=== Setting up project ===${AnsiColors.RESET}")
            println("${AnsiColors.BLUE}GitHub Repository:${AnsiColors.RESET} $githubRepo")
            println()

            val projectConfig = ProjectConfig(githubRepository = githubRepo)
            configRepository.saveProjectConfig(projectConfig)
            val configPath = configRepository.getProjectConfigFilePath()
            println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Project configuration saved to $configPath")
            println()
        }

        // kinfra.yamlを読み込むか作成
        setupKinfraConfig()

        println("${AnsiColors.BLUE}=== Bitwarden Login ===${AnsiColors.RESET}")
        println()

        // 認証方式を選択
        println("${AnsiColors.BLUE}Select authentication method:${AnsiColors.RESET}")
        println("  1) Secret Manager (BWS_ACCESS_TOKEN) - Recommended")
        println("  2) CLI (bw) - Legacy")
        print("Choice [1]: ")
        val choice = readLine()?.takeIf { it.isNotBlank() } ?: "1"

        return when (choice) {
            "1" -> setupSecretManagerToken()
            "2" -> setupBwCli()
            else -> {
                println("${AnsiColors.RED}Invalid choice${AnsiColors.RESET}")
                1
            }
        }
    }

    private fun setupSecretManagerToken(): Int {
        println()
        println("${AnsiColors.BLUE}=== Secret Manager Setup ===${AnsiColors.RESET}")
        println()

        // Check if token already exists
        val tokenFile = File(BWS_TOKEN_FILE)
        if (tokenFile.exists()) {
            println("${AnsiColors.YELLOW}BWS_ACCESS_TOKEN file already exists${AnsiColors.RESET}")
            print("Overwrite? (y/N): ")
            val overwrite = readLine()?.lowercase()
            if (overwrite != "y" && overwrite != "yes") {
                println("${AnsiColors.BLUE}Keeping existing token${AnsiColors.RESET}")
                return 0
            }
        }

        println("${AnsiColors.BLUE}Enter your BWS_ACCESS_TOKEN:${AnsiColors.RESET}")
        println("${AnsiColors.YELLOW}(You can generate this from Bitwarden Web Vault > Secret Manager)${AnsiColors.RESET}")
        print("Token: ")

        val token = System.console()?.readPassword()?.let { String(it) } ?: run {
            // If console is not available, read from standard input
            readLine()
        }

        if (token.isNullOrBlank()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Token cannot be empty")
            return 1
        }

        // Save token to file
        try {
            tokenFile.writeText(token)
            tokenFile.setReadable(false, false)
            tokenFile.setReadable(true, true)
            tokenFile.setWritable(false, false)
            tokenFile.setWritable(true, true)

            println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Token saved to ${tokenFile.absolutePath}")
            println()
            println("${AnsiColors.BLUE}The token will be automatically loaded on next run.${AnsiColors.RESET}")
            println("${AnsiColors.BLUE}You can also set it manually:${AnsiColors.RESET}")
            println("  export BWS_ACCESS_TOKEN=\$(cat ${BWS_TOKEN_FILE})")

            return 0
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to save token: ${e.message}")
            return 1
        }
    }

    private fun setupKinfraConfig() {
        println("${AnsiColors.BLUE}=== Kinfra Configuration ===${AnsiColors.RESET}")

        if (configRepository.kinfraConfigExists()) {
            println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Found kinfra.yaml")
            try {
                val config = configRepository.loadKinfraConfig()
                if (config != null) {
                    println("${AnsiColors.BLUE}Project:${AnsiColors.RESET} ${config.project.name}")
                    if (config.project.repository.isNotEmpty()) {
                        println("${AnsiColors.BLUE}Repository:${AnsiColors.RESET} ${config.project.repository}")
                    }
                    println()
                } else {
                    println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to parse kinfra.yaml")
                    println()
                }
            } catch (e: Exception) {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to read kinfra.yaml: ${e.message}")
                println()
            }
        } else {
            println("${AnsiColors.YELLOW}kinfra.yaml not found${AnsiColors.RESET}")
            println("${AnsiColors.BLUE}Creating default kinfra.yaml...${AnsiColors.RESET}")

            val projectConfig = configRepository.loadProjectConfig()
            val defaultConfig = KinfraConfig(
                project = ProjectInfo(
                    repository = projectConfig.githubRepository ?: ""
                )
            )

            try {
                configRepository.saveKinfraConfig(defaultConfig)
                println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Created kinfra.yaml")
                println("${AnsiColors.BLUE}You can customize it later by editing the file${AnsiColors.RESET}")
                println()
            } catch (e: Exception) {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to create kinfra.yaml: ${e.message}")
                println()
            }
        }
    }

    private fun setupBwCli(): Int {
        println()
        println("${AnsiColors.BLUE}=== Bitwarden CLI Setup ===${AnsiColors.RESET}")
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

        // Check if already logged in
        if (bitwardenRepository.isLoggedIn()) {
            println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Already logged in to Bitwarden")
            println()
            println("${AnsiColors.BLUE}Unlocking vault...${AnsiColors.RESET}")
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

            // Save session to file
            try {
                val sessionFile = File(SESSION_FILE)
                sessionFile.writeText(session)
                sessionFile.setReadable(false, false)
                sessionFile.setReadable(true, true)
                sessionFile.setWritable(false, false)
                sessionFile.setWritable(true, true)
                println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Vault unlocked successfully")
                println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Session saved to ${sessionFile.absolutePath}")
                println()
                println("${AnsiColors.BLUE}To use the session, run:${AnsiColors.RESET}")
                println("  export BW_SESSION=\$(cat ${SESSION_FILE})")
            } catch (e: Exception) {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to save session: ${e.message}")
                return 1
            }
            return 0
        }

        println("${AnsiColors.YELLOW}Not logged in to Bitwarden.${AnsiColors.RESET}")
        println("${AnsiColors.BLUE}Please run:${AnsiColors.RESET} bw login")
        println()
        println("After logging in, run this command again to unlock your vault.")

        return 1
    }

    override fun getDescription(): String {
        return "Login to Bitwarden and store session token"
    }
}
